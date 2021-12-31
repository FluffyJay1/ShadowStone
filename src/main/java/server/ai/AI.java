package server.ai;

import java.util.*;

import network.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.playeraction.*;
import server.resolver.*;
import utils.WeightedSampler;

/**
 * This AI class is all about making decisions for a player in a game. The AI
 * outputs playeractions and receives updates about the board through a
 * DataStream.
 * 
 * @author Michael
 *
 */
public class AI extends Thread {
    private static final boolean DEBUG_PRINT = true;

    // some baseline value things for value estimation of effects
    public static final double VALUE_PER_DAMAGE = 1;

    public static final double VALUE_PER_HEAL = 0.5;

    public static final double VALUE_PER_CARD_IN_HAND = 1;

    public static final double VALUE_PER_1_1_STATS = 1;

    /*
     * We can't expect the AI to traverse every single possible node in the decision
     * tree before making a move (especially considering rng), so after a certain
     * depth we will only sample some of the decisions and extrapolate the strength
     * of the overall turn from there. When the AI commits its decisions and reaches
     * this depth again, we will have to re-evaluate which action is best.
     */
    // The minimum depth for sampling to occur (if rng happens, we do sampling regardless)
    private static final int SAMPLING_MIN_DEPTH = 1;

    // After this depth, just kinda call it
    private static final int SAMPLING_MAX_DEPTH = 30;

    // The minimum number of branches to sample at each level
    private static final int SAMPLING_MIN_SAMPLES = 1;

    // The maximum number of branches to sample at min depth
    private static final int SAMPLING_MAX_SAMPLES = 24;

    // How much the max number of samples gets multiplied by per level
    private static final double SAMPLING_MAX_SAMPLES_MULTIPLIER = 0.6;

    // Proportion of total possible actions that we sample at the start
    // should be greater than 1 lol
    private static final double SAMPLING_START_SAMPLE_RATE = 2.5;

    // Multiplier of sample rate at each depth
    private static final double SAMPLING_SAMPLE_RATE_MULTIPLIER = 0.75;

    // The more branches a node has, the less in-depth it should sample them
    private static final double SAMPLING_SAMPLE_RATE_OVERCROWD_MULTIPLIER = 0.96;

    // After an rng event, we shouldn't care too much about evaluating in detail
    private static final double SAMPLING_SAMPLE_RATE_RNG_PENALTY_MULTIPLIER = 0.85;

    // how much to multiply the penalty multiplier per extra trial
    private static final double SAMPLING_SAMPLE_RATE_RNG_PENALTY_REDUCTION = 0.7;

    // When revisiting nodes, tolerate lower detail levels up to a certain amount
    private static final double REEVALUATION_MAX_SAMPLE_RATE_DIFF = 0.20;

    // Same idea as sample rate, but for rng
    private static final int RNG_MAX_TRIALS = 12;
    private static final int RNG_MIN_TRIALS = 3;

    // how many less trials each subsequent depth gets
    private static final int RNG_TRIAL_REDUCTION = 2;

    /*
     * Some actions are more important than others
     * when we sample decisions, some should be sampled first more often
     * things that require targeting (such as battlecry cards) have a separate
     * action for each set of targets, but the weights of these actions should
     * sum up to something per card
     */
    private static final double PLAY_CARD_TOTAL_WEIGHT = 4;

    // how much extra weight for the play card action, scales off of the card's cost
    private static final double PLAY_CARD_COST_WEIGHT_MULTIPLIER = 0.5;

    private static final double UNLEASH_TOTAL_WEIGHT = 12;

    private static final double ATTACK_TOTAL_WEIGHT = 2;

    // how much extra weight to put into the attack action, scales off of the minion's attack
    private static final double ATTACK_WEIGHT_MULTIPLIER = 1;

    // bonus for attacking the leader
    private static final double ATTACK_TARGET_LEADER_MULTIPLIER = 3;

    private static final double END_TURN_WEIGHT = 1;

    // Statistics to gauge AI evaluation speed
    private final int[] width = new int[SAMPLING_MAX_DEPTH + 1];
    private final int[] maxBranches = new int[SAMPLING_MAX_DEPTH + 1];
    private final int[] cacheHits = new int[SAMPLING_MAX_DEPTH + 1];
    private int totalEvaluated;
    private int totalCacheHits;
    private int totalReevaluations;

    // Map board state to cached AI calculations
    private final Map<String, DeterministicBoardStateNode> nodeMap;

    // When traversing the decision tree, keep track of which nodes we have already traversed, to avoid cycles
    // highly unlikely that we need this but just to be complete
    private final Set<DeterministicBoardStateNode> traversedNodes;

    int difficulty;
    Board b;
    final DataStream dslocal;
    List<String> actionSendQueue;
    boolean waitForEvents, finishedTurn;

    public AI(DataStream dslocal, int team, int difficulty) {
        this.difficulty = difficulty;
        this.dslocal = dslocal;
        this.b = new Board(team);
        this.actionSendQueue = new LinkedList<>();
        this.nodeMap = new HashMap<>();
        this.traversedNodes = new HashSet<>();
    }

    @Override
    public void run() {
        while (this.b.winner == 0) {
            this.readDataStream();
            if (this.b.currentPlayerTurn == this.b.localteam && !this.finishedTurn && !this.waitForEvents) {
                if (this.actionSendQueue.isEmpty() && this.b.winner == 0) {
                    this.AIThink();
                }
                this.sendNextAction();
                this.waitForEvents = true;
            }
        }
    }

    private void readDataStream() {
        if (this.dslocal.ready()) {
            MessageType mtype = this.dslocal.receive();
            switch (mtype) {
            case EVENT:
                String eventstring = this.dslocal.readEvent();
                this.b.parseEventString(eventstring);
                this.waitForEvents = false;
                if (this.b.currentPlayerTurn == this.b.localteam) {
                    this.finishedTurn = false;
                }
                break;
            case BOARDRESET:
                this.b = new Board(this.b.localteam);
                this.actionSendQueue = new LinkedList<>();
                this.waitForEvents = true;
            default:
                break;
            }
        }
    }

    private void AIThink() {
        if (DEBUG_PRINT) System.out.println("Starting to think...");
        for (int i = 0; i < this.width.length; i++) {
            this.width[i] = 0;
            this.maxBranches[i] = 0;
            this.cacheHits[i] = 0;
        }
        this.totalEvaluated = 0;
        this.totalCacheHits = 0;
        this.totalReevaluations = 0;
        List<String> actionStack = new LinkedList<>();
        long start = System.nanoTime();
        DeterministicBoardStateNode dbsn = this.getBestTurn(this.b.localteam, 0, SAMPLING_START_SAMPLE_RATE, Double.POSITIVE_INFINITY, false);
        if (dbsn == null) {
            System.out.println("AIThink returned from a null best turn, sadge");
            return;
        }
        double time = (System.nanoTime() - start) / 1000000000.;
        DeterministicBoardStateNode temp = dbsn;
        while (temp.team == this.b.localteam && (temp.isFullyEvaluated() || temp.lethal)) {
            String nextAction = temp.getMax().action;
            actionStack.add(nextAction);
            BoardStateNode next = temp.branches.get(nextAction);
            if (next instanceof DeterministicBoardStateNode) {
                temp = (DeterministicBoardStateNode) next;
            } else {
                break;
            }
        }
        if (actionStack.isEmpty()) {
            System.out.println("AIThink produced no actions!");
        }

        if (DEBUG_PRINT) {
            System.out.println("Time taken: " + time);
            System.out.println("Start score: " + evaluateAdvantage(this.b, this.b.localteam));
            System.out.println("Score achieved: " + dbsn.getMax().score);
            System.out.printf("%-6s %-6s %-6s %-6s\n", "Depth", "Width", "Cache", "Brnchs");
            for (int i = 0; i < this.width.length; i++) {
                if (this.width[i] == 0) {
                    break;
                }
                System.out.printf("%6d %6d %6d %6d\n", i, this.width[i], this.cacheHits[i], this.maxBranches[i]);
            }
            System.out.printf("Total: %d, cache hit rate: %.2f\n",
                    this.totalEvaluated, (float) this.totalCacheHits / this.totalEvaluated);
            System.out.printf("%d reevaluations, %.2f reevaluation rate\n", this.totalReevaluations, (float) this.totalReevaluations / this.totalCacheHits);

            System.out.println("Nodes Traversed---");
            temp = dbsn;
            while (true) {
                String nextAction = temp.getMax().action;
                System.out.println(temp.toString());
                BoardStateNode next = temp.branches.get(nextAction);
                if (next instanceof DeterministicBoardStateNode) {
                    temp = (DeterministicBoardStateNode) next;
                } else {
                    break;
                }
            }
            System.out.println("------------------");
        }
        this.actionSendQueue.addAll(actionStack);
    }

    private void sendNextAction() {
        // TODO: check if action is still valid
        if (this.actionSendQueue.isEmpty()) {
            // invalid state, maybe something is up
            System.out.println("AI attempt to send action without any actions in action queue");
            this.finishedTurn = true;
            return;
        }
        String action = this.actionSendQueue.remove(0);
        this.dslocal.sendPlayerAction(action);
        StringTokenizer st = new StringTokenizer(action);
        if (Integer.parseInt(st.nextToken()) == EndTurnAction.ID) {
            this.finishedTurn = true;
            this.nodeMap.clear();
        }
    }

    /**
     * Given the current board state, get the sequence of player actions that
     * maximizes the advantage of a player. Should the turn be complicated by RNG or
     * a large number of possibilities, get only the first few actions that may lead
     * to the greatest advantage. Will end the turn with a EndTurnAction. Has the
     * side effect of populating the AI decision tree, which caches results of
     * previous AI evaluation, valid until the AI ends its turn. Returns null if no
     * actions are possible, i.e. if it's the wrong player's turn or the game is
     * over.
     * 
     * @param team         The team of the player to evaluate for
     * @param depth        Passes the current depth in the decision tree, should
     *                     start at 0
     * @param sampleRate   The proportion of total actions to sample
     * @param maxSamples   Max number of samples to use
     * @param filterLethal Flag to only traverse actions that may result in lethal
     * @return A BoardStateNode object that has been populated, with data such as
     *         maxScore and maxAction, for the current board state
     */
    private DeterministicBoardStateNode getBestTurn(int team, int depth, double sampleRate, double maxSamples,
            boolean filterLethal) {
        // no turn possible
        if (this.b.currentPlayerTurn != team || this.b.winner != 0) {
            return null;
        }
        String state = this.b.stateToString();
        DeterministicBoardStateNode dbsn;
        boolean cacheHit = false;
        if (!this.nodeMap.containsKey(state)) {
            dbsn = new DeterministicBoardStateNode(team,
                    evaluateAdvantage(this.b, team),
                    state,
                    filterLethal ? this.getPossibleLethalActions(team) : this.getPossibleActions(team));
            dbsn.sampleRate = sampleRate;
            this.nodeMap.put(state, dbsn);
        } else {
            dbsn = this.nodeMap.get(state);
            cacheHit = true;
            if (this.traversedNodes.contains(dbsn)) {
                System.err.println("AI encountered a cycle somehow");
                return dbsn;
            }
        }
        this.traversedNodes.add(dbsn);
        // start sampling
        int numSamples = (int) Math.round(Math.min(dbsn.totalBranches * sampleRate, maxSamples));
        numSamples = Math.max(numSamples, SAMPLING_MIN_SAMPLES);
        numSamples = Math.min(numSamples, dbsn.totalBranches);
        // if we are revisiting this node, but it needs to be re-evaluated at a better detail level
        if (cacheHit && dbsn.sampleRate < sampleRate - REEVALUATION_MAX_SAMPLE_RATE_DIFF) {
            dbsn.resetEvaluationOrder();
            dbsn.sampleRate = sampleRate;
            this.totalReevaluations++;
        }

        // some statistics for debugging purposes
        this.maxBranches[depth] = Math.max(this.maxBranches[depth], numSamples);
        this.width[depth]++;
        this.totalEvaluated++;
        if (cacheHit) {
            this.cacheHits[depth]++;
            this.totalCacheHits++;
        }
        while (dbsn.evaluatedBranches < numSamples) {
            String actionString = dbsn.nextBranchToEvaluate();
            TraversalResult traverse = this.traverseAction(dbsn, actionString, depth, sampleRate, maxSamples,
                    filterLethal, null);
            if (traverse.rng) {
                // rng, re-evaluate action many times, channel an average score into an RNGBoardStateNode
                int trials = Math.max(RNG_MAX_TRIALS - depth * RNG_TRIAL_REDUCTION, RNG_MIN_TRIALS);
                RNGBoardStateNode nextBsn = (RNGBoardStateNode) dbsn.branches.get(actionString); // trust
                if (nextBsn != null) {
                    // if previously evaluated this rng branch, see if we can add some trials to it
                    trials = Math.max(1, trials - nextBsn.trials);
                } else {
                    nextBsn = new RNGBoardStateNode(team);
                    nextBsn.addTrial(traverse.next);
                }
                for (int j = 1; j < trials; j++) {
                    traverse = this.traverseAction(dbsn, actionString, depth, sampleRate, maxSamples, filterLethal, nextBsn);
                    nextBsn.addTrial(traverse.next);
                }
                dbsn.logEvaluation(actionString, nextBsn);
            } else {
                // not rng, whatever
                dbsn.logEvaluation(actionString, traverse.next);
                if (dbsn.lethal) {
                    break;
                }
            }
        }
        this.traversedNodes.remove(dbsn);
        return dbsn;
    }

    /**
     * Helper method (for getBestTurn) to perform a traversal of the decision tree,
     * recursively exploring the subtree. Assumes that the board is in the
     * pre-traversal state, i.e. the action has not been carried out. When this
     * method returns, the board should have returned to the state that it was
     * in when this was called. Has the side effect of modifying the current
     * node's lethal flag, if this action can lead to guaranteed lethal (i.e.
     * only if the action doesn't result in rng).
     * 
     * @param current      The node we start from
     * @param action       The action to perform
     * @param depth        The current traversal depth
     * @param sampleRate   The proportion of total actions we sampled
     * @param maxSamples   The max number of samples we used before
     * @param filterLethal Flag to only traverse actions that may result in lethal
     * @param nextRNG      If we know the action results in rng, this is the rng node seen by the tree, for reference
     * @return The BoardStateNode at the end of the edge traversed
     */
    private TraversalResult traverseAction(DeterministicBoardStateNode current, String action, int depth, double sampleRate, double maxSamples,
            boolean filterLethal, RNGBoardStateNode nextRNG) {
        ResolutionResult result = this.b.executePlayerAction(new StringTokenizer(action));
        BoardStateNode node;
        double nextMaxSamples = maxSamples;
        double nextSampleRate = sampleRate;
        if (depth + 1 >= SAMPLING_MIN_DEPTH) {
            if (depth + 1 == SAMPLING_MIN_DEPTH) {
                nextMaxSamples = SAMPLING_MAX_SAMPLES;
            } else {
                nextMaxSamples = maxSamples * SAMPLING_MAX_SAMPLES_MULTIPLIER;
            }
            nextSampleRate = sampleRate * SAMPLING_SAMPLE_RATE_MULTIPLIER * Math.pow(SAMPLING_SAMPLE_RATE_OVERCROWD_MULTIPLIER, current.totalBranches);
        }
        if (result.rng) {
            // the number of trials we've already done to get to this node
            int cumTrials = 0;
            if (nextRNG != null) {
                cumTrials += nextRNG.getCount(this.nodeMap.get(this.b.stateToString()));
            }
            nextSampleRate *= 1 - (SAMPLING_SAMPLE_RATE_RNG_PENALTY_MULTIPLIER * Math.pow(SAMPLING_SAMPLE_RATE_RNG_PENALTY_REDUCTION, cumTrials));
        }
        if (this.b.winner != 0 || depth == SAMPLING_MAX_DEPTH) {
            // no more actions can be taken
            node = new TerminalBoardStateNode(current.team, evaluateAdvantage(this.b, current.team));
            if (!result.rng && this.b.winner == current.team) {
                current.lethal = true; // the current node has an action that results in a lethal 100% of the time
            }
        } else if (Integer.parseInt(action.substring(0, 1)) == EndTurnAction.ID || this.b.currentPlayerTurn != current.team) {
            // assess what's the worst the opponent can do
            if (filterLethal) {
                // not part of my pay grade
                node = new TerminalBoardStateNode(current.team * -1, evaluateAdvantage(this.b, current.team * -1));
            } else {
                node = this.getBestTurn(current.team * -1, depth + 1, nextSampleRate, nextMaxSamples, true);
            }
        } else {
            DeterministicBoardStateNode dbsn = this.getBestTurn(current.team, depth + 1, nextSampleRate, nextMaxSamples, filterLethal);
            assert dbsn != null;
            if (dbsn.lethal) {
                current.lethal = true; // the current node has an action that leads to a node that eventually results in lethal
            }
            node = dbsn;
        }
        List<Event> undoStack = new LinkedList<>(result.events);
        while (!undoStack.isEmpty()) {
            undoStack.get(undoStack.size() - 1).undo();
            undoStack.remove(undoStack.size() - 1);
        }
        String stateAfterUndo = this.b.stateToString();
        if (!current.state.equals(stateAfterUndo)) {
            System.out.println(
                    "Discrepancy after executing " + action + ", rng = " + result.rng + ", depth = " + depth);
            for (Event e : result.events) {
                System.out.print(e.toString());
            }
            System.out.println("Before:");
            System.out.println(current.state);
            System.out.println("After:");
            System.out.println(stateAfterUndo);
            throw new RuntimeException();
        }
        return new TraversalResult(node, result.rng);
    }

    /**
     * Given the current board state, attempts to find every possible action that a
     * player on this team can make.
     * 
     * @param team The team to evaluate for
     * @return a some weighted set of possible actions taken by the AI, to be sampled
     */
    private WeightedSampler<String> getPossibleActions(int team) {
        if (this.b.currentPlayerTurn != team || this.b.winner != 0) {
            return null;
        }
        Player p = this.b.getPlayer(team);
        WeightedSampler<String> poss = new WeightedSampler<>();
        // playing cards & selecting targets
        List<Card> hand = p.hand.cards;
        for (Card c : hand) {
            // TODO make it consider board positioning
            if (p.canPlayCard(c)) {
                double totalWeight = PLAY_CARD_TOTAL_WEIGHT + PLAY_CARD_COST_WEIGHT_MULTIPLIER * c.finalStatEffects.getStat(EffectStats.COST);
                if (!c.getBattlecryTargets().isEmpty()) {
                    List<List<Target>> targetSearchSpace = this.getPossibleListTargets(c.getBattlecryTargets());
                    for (List<Target> targets : targetSearchSpace) {
                        poss.add(new PlayCardAction(p, c, 0, Target.listToString(targets)).toString(), totalWeight / targetSearchSpace.size());
                    }
                } else { // no targets to set
                    poss.add(new PlayCardAction(p, c, 0, "0").toString(), totalWeight);
                }
            }
        }
        List<BoardObject> minions = this.b.getBoardObjects(team, false, true, false);
        for (BoardObject b : minions) {
            Minion m = (Minion) b;
            // unleashing cards & selecting targets
            if (p.canUnleashCard(m)) {
                double totalWeight = UNLEASH_TOTAL_WEIGHT; // can't really make any assumptions about which unleashes are better than others
                if (!m.getUnleashTargets().isEmpty()) {
                    List<List<Target>> targetSearchSpace = this.getPossibleListTargets(m.getUnleashTargets());
                    for (List<Target> targets : targetSearchSpace) {
                        poss.add(new UnleashMinionAction(p, m, Target.listToString(targets)).toString(), totalWeight / targetSearchSpace.size());
                    }
                } else { // no targets to set
                    poss.add(new UnleashMinionAction(p, m, "0").toString(), totalWeight);
                }
            }
            // minion attack
            if (m.canAttack()) {
                double totalWeight = ATTACK_TOTAL_WEIGHT + ATTACK_WEIGHT_MULTIPLIER * m.finalStatEffects.getStat(EffectStats.ATTACK);
                List<Minion> searchSpace = m.getAttackableTargets();
                for (Minion target : searchSpace) {
                    double bonus = target instanceof Leader ? ATTACK_TARGET_LEADER_MULTIPLIER : 1;
                    poss.add(new OrderAttackAction(m, target).toString(), bonus * totalWeight / searchSpace.size());
                }
            }
        }
        // ending turn
        poss.add(new EndTurnAction(team).toString(), END_TURN_WEIGHT);
        return poss;
    }

    /**
     * Given the current board state, return the obvious actions that could lead to
     * a lethal. Intended to be a subset of all possible actions that are openly
     * visible on the board, i.e. unleashing minions and attacking face.
     * 
     * @param team The team to evaluate for
     * @return Some weighted set of actions that the team could take that could lead to lethal, to be sampled
     */
    private WeightedSampler<String> getPossibleLethalActions(int team) {
        if (this.b.currentPlayerTurn != team || this.b.winner != 0) {
            return null;
        }
        Player p = this.b.getPlayer(team);
        WeightedSampler<String> poss = new WeightedSampler<>();
        List<Minion> minions = this.b.getMinions(team, false, true);
        boolean enemyWard = false;
        for (Minion m : this.b.getMinions(team * -1, false, true)) {
            if (m.finalStatEffects.getStat(EffectStats.WARD) > 0) {
                enemyWard = true;
                break;
            }
        }
        for (Minion m : minions) {
            if (p.canUnleashCard(m)) {
                double totalWeight = UNLEASH_TOTAL_WEIGHT; // can't really make any assumptions about which unleashes are better than others
                if (!m.getUnleashTargets().isEmpty()) {
                    List<List<Target>> targetSearchSpace = this.getPossibleListTargets(m.getUnleashTargets());
                    for (List<Target> targets : targetSearchSpace) {
                        poss.add(new UnleashMinionAction(p, m, Target.listToString(targets)).toString(), totalWeight / targetSearchSpace.size());
                    }
                } else { // no targets to set
                    poss.add(new UnleashMinionAction(p, m, "0").toString(), totalWeight);
                }
            }
        }
        if (enemyWard) {
            // find ways to break through the wards
            for (Minion m : minions) {
                if (m.canAttack()) {
                    double totalWeight = ATTACK_TOTAL_WEIGHT + ATTACK_WEIGHT_MULTIPLIER * m.finalStatEffects.getStat(EffectStats.ATTACK);
                    List<Minion> searchSpace = m.getAttackableTargets();
                    for (Minion target : searchSpace) {
                        poss.add(new OrderAttackAction(m, target).toString(), totalWeight / searchSpace.size());
                    }
                }
            }
        } else {
            // just go face
            Leader enemyFace = this.b.getPlayer(team * -1).leader;
            for (Minion m : minions) {
                if (m.canAttack(enemyFace)) {
                    double totalWeight = ATTACK_TOTAL_WEIGHT + ATTACK_WEIGHT_MULTIPLIER * m.finalStatEffects.getStat(EffectStats.ATTACK);
                    poss.add(new OrderAttackAction(m, enemyFace).toString(), totalWeight);
                }
            }
        }
        poss.add(new EndTurnAction(team).toString(), END_TURN_WEIGHT);
        return poss;
    }

    /**
     * Given the current board state and a targeting requirement to fulfill,
     * recursively finds every possible combination of targets, returns null if it
     * cannot fully target because startInd was too large
     * 
     * @param t           The target to fill
     * @param searchSpace the list of cards to search through that are possible
     *                    targets, for optimization purposes, usually it's just
     *                    this.b.getTargetableCards(t)
     * @param startInd    the first index of targets in the searchspace that haven't
     *                    been considered, for optimization purposes, usually it's
     *                    just 0
     * @return A list of possible targets
     */
    private List<Target> getPossibleTargets(Target t, List<Card> searchSpace, int startInd) {
        // TODO TEST THAT IT DOESN'T SEARCH THE SAME COMBINATION TWICE
        List<Target> poss = new LinkedList<>();
        if (t.isReady()) {
            poss.add(t);
            return poss;
        }
        if (startInd >= searchSpace.size()) {
            System.out.println("this shouldn't happen lmao");
        }
        for (int i = startInd; i < searchSpace.size() - (t.maxtargets - t.getTargets().size() - 1); i++) {
            Card c = searchSpace.get(i);
            if (!t.getTargets().contains(c)) {
                Target copy = t.clone();
                copy.addCard(c);
                List<Target> subspace = this.getPossibleTargets(copy, searchSpace, i + 1);
                poss.addAll(subspace);
            }
        }
        return poss;
    }

    /**
     * Given the current board state and a list of targets, recursively finds every
     * possible combination of target combos, e.g. if a card has 3 battlecry effects
     * each with their own targeting scheme, this will find every possible
     * combination of targets of those 3 effects, like (0, 1, 2), (0, 1, 3), (0, 2,
     * 1), etc.
     * 
     * @param list The list of targets to fill
     * @return A list of possible sets of targets. Each element in the list is a set
     *         of targets to be used by the card.
     */
    private List<List<Target>> getPossibleListTargets(List<Target> list) {
        // TODO OPTIMIZE SO IT DOESN'T SEARCH THE SAME COMBINATION TWICE
        List<List<Target>> poss = new LinkedList<>();
        if (list.isEmpty()) {
            return poss;
        }
        list.get(0).reset();
        List<Target> searchspace = this.getPossibleTargets(list.get(0), this.b.getTargetableCards(list.get(0)), 0);
        for (Target t : searchspace) {
            List<Target> posscombo = new LinkedList<>();
            posscombo.add(t.clone());
            for (List<Target> subspace : this.getPossibleListTargets(list.subList(1, list.size()))) {
                posscombo.addAll(subspace);
            }
            poss.add(posscombo);
        }
        return poss;
    }

    /**
     * Attempts to estimate the advantage of a player in terms of mana value. This
     * is the value that gets maximized by the ai. These values should be
     * symmetrical, i.e. evauateAdvantage(b, 1) == -evaluateAdvantage(b, -1)
     * 
     * @param b    The board
     * @param team the team to evaluate for
     * @return the advantage quantized as mana
     */
    public static double evaluateAdvantage(Board b, int team) {
        return evaluateVictory(b, team) + evaluateMana(b, team) + evaluateSurvivability(b, team)
                + evaluateBoard(b, team) + evaluateHand(b, team) - evaluateSurvivability(b, team * -1)
                - evaluateBoard(b, team * -1) - evaluateHand(b, team * -1);
    }

    /**
     * Indicator that a player has achieved victory. Is probably a bit redundant
     * with how the AI evaluates lethal but whatever
     * 
     * @param b    The board
     * @param team the team to evaluate for
     * @return a large number in favor of the winning team
     */
    public static double evaluateVictory(Board b, int team) {
        if (b.winner == team) {
            return 999999.;
        } else if (b.winner == team * -1) {
            return -999999.;
        } else {
            return 0;
        }
    }

    /**
     * Health is a resource, attempts to evaluate the mana worth of having high
     * hp/opponent at low hp, factoring in enemy minions and friendly wards. The
     * formula is mana = 6ln(ehp), making a 12hp heal worth about 4 mana when at 13
     * hp, equivalent to greater healing potion, and a 6hp nuke worth about 4 mana
     * when opponent is at 12 hp, equivalent to fireball
     * 
     * @param b    The board
     * @param team the team to evaluate for
     * @return The approximate mana value of that leader's survivability
     */
    public static double evaluateSurvivability(Board b, int team) {
        Leader l = b.getPlayer(team).leader;
        if (l == null) {
            return 0;
        }
        if (l.health <= 0) { // if dead
            return -99999 + l.health; // u dont want to be dead
        }
        int potentialDamage = 0, threatenDamage = 0, ehp = l.health, attackers = 0, defenders = 0;
        for (BoardObject bo : b.getBoardObjects(team * -1, false, true, false)) {
            // TODO add if can attack check
            Minion m = (Minion) bo;
            // TODO factor in damage limiting effects like durandal
            if (m.canAttack()) {
                potentialDamage += m.finalStatEffects.getStat(EffectStats.ATTACK)
                        * (m.finalStatEffects.getStat(EffectStats.ATTACKS_PER_TURN) - m.attacksThisTurn);
            }
            threatenDamage += m.finalStatEffects.getStat(EffectStats.ATTACK)
                    * m.finalStatEffects.getStat(EffectStats.ATTACKS_PER_TURN);
            attackers += m.finalStatEffects.getStat(EffectStats.ATTACKS_PER_TURN);
        }
        for (BoardObject bo : b.getBoardObjects(team, false, true, false)) {
            Minion m = (Minion) bo;
            if (m.finalStatEffects.getStat(EffectStats.WARD) > 0) {
                ehp += m.health;
                defenders++;
            }
        }

        /*
        Turn-dependent evaluation is a bad idea, because of evaluation can get cut off in the middle
        e.g. one branch may look better than another branch, because the first branch didn't evaluate
        deep enough where an end turn was the only option
        
        this following branch is experimental, so the AI doesn't think a turn sucks because it 
        reached max depth before being able to attack with its minions
        */
        if (b.currentPlayerTurn != team) {
            threatenDamage = potentialDamage;
        }
        // if there are more defenders than attacks, then minions shouldn't be
        // able to touch face
        if (defenders >= attackers) {
            ehp = Math.max(ehp - threatenDamage, l.health);
        } else {
            if (threatenDamage >= ehp) {
                // they're threatening lethal if i dont do anything
                return -30 + ehp - threatenDamage;
            }
            ehp -= threatenDamage;
        }
        return 6 * Math.log(ehp);
    }

    /**
     * Attempts to put a mana value on a boardstate, basically just the sum of the
     * values of each board object
     * 
     * @param b    The board
     * @param team the team to evaluate for
     * @return the approximate mana value
     */
    public static double evaluateBoard(Board b, int team) {
        double total = 0;
        for (BoardObject bo : b.getBoardObjects(team)) {
            total += bo.getValue();
        }
        return total;
    }

    /**
     * More cards in hand is better, but you also want playable stuff. The power of
     * a card is value/(cost + 1), which usually comes to about 1 mana per card. A
     * more powerful hand would have higher value cards with lower cost.
     * 
     * @param b    The board
     * @param team the team to evaluate for. can cheat by looking at opponents hand
     *             lol
     * @return the mana value of having these cards in hand
     */
    public static double evaluateHand(Board b, int team) {
        Hand hand = b.getPlayer(team).hand;
        double totalPower = 0;
        for (Card c : hand.cards) {
            totalPower += c.getValue() / (c.finalStatEffects.getStat(EffectStats.COST) + 1);
        }
        return totalPower;
    }

    /**
     * This metric attempts to quantify ramping as an advantage
     * 
     * @param b    The board
     * @param team The team to evaluate for
     * @return Mana value of difference in max mana
     */
    public static double evaluateMana(Board b, int team) {
        // basically calculates how much extra mana one side will eventually get to play with
        int localmax = b.getPlayer(team).maxmana;
        int localmaxmax = b.getPlayer(team).maxmaxmana;
        int enemymax = b.getPlayer(-team).maxmana;
        int enemymaxmax = b.getPlayer(-team).maxmaxmana;
        return (localmax - enemymax) * Math.max(localmaxmax - localmax, enemymaxmax - enemymax);
    }

    // kekl
    private static class TraversalResult {
        BoardStateNode next;
        boolean rng;
        TraversalResult(BoardStateNode next, boolean rng) {
            this.next = next;
            this.rng = rng;
        }
    }
}
