package server.ai;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import network.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.*;
import server.event.eventburst.EventBurst;
import server.playeraction.*;
import server.resolver.*;
import utils.SelectRandom;
import utils.WeightedOrderedSampler;
import utils.WeightedRandomSampler;
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

    public static final double VALUE_OF_STEALTH = 1;

    public static final double VALUE_OF_SHIELD = 1;

    public static final double VALUE_OF_DESTROY = 4;

    public static final double VALUE_OF_BANE = 1.5;

    public static final double VALUE_OF_SPELLBOOST = 1.5;

    public static final double VALUE_OF_DISCARD = -0.75;

    public static final double VALUE_PER_RAMP = 1.5;

    public static final double VALUE_PER_SHADOW = 0.5;

    public static final double VALUE_OF_BANISH = 5;

    public static final double VALUE_OF_ELUSIVE = 1;

    public static final double VALUE_OF_UNLEASH = 2;

    public static final double VALUE_OF_WARD = 1;

    public static final double VALUE_OF_FREEZING_TOUCH = 1;

    public static final double VALUE_OF_FREEZE = 0.5;

    public static final double VALUE_OF_POISONOUS = 2;

    public static final double VALUE_OF_LIFESTEAL = 1;

    public static final double VALUE_OF_MUTE = 1;

    public static final double VALUE_OF_BOUNCE_ALLIED = 0.5;
    public static final double VALUE_OF_BOUNCE_ENEMY = 2.5;

    public static final double VALUE_PER_ARMOR = 1.5;

    public static final double VALUE_OF_INTIMIDATE = 1;

    public static final double VALUE_OF_STALWART = 1;

    public static final double VALUE_OF_REPEL = 1;

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

    // extra weight for unleash, scales off of presence value
    private static final double UNLEASH_WEIGHT_PER_PRESENCE = 1;

    private static final double ATTACK_TOTAL_WEIGHT = 6;

    // how much extra weight to put into the attack action, scales off of the minion's attack
    private static final double ATTACK_WEIGHT_MULTIPLIER = 2;

    // How much to multiply the weight per extra damage overkill from attacking a low health minion
    // encourage better trades
    private static final double ATTACK_WEIGHT_OVERKILL_PENALTY = 0.8;

    // bonus for attacking the leader
    private static final double ATTACK_TARGET_LEADER_MULTIPLIER = 3;

    private static final double END_TURN_WEIGHT = 1;

    // min score diff to trigger an emote
    private static final double EMOTE_SCORE_CHANGE_THRESHOLD = 15;
    private static final double EMOTE_SCORE_CHANGE_THRESHOLD_PER_TURN = 1;

    // Statistics to gauge AI evaluation speed
    private final int[] width = new int[SAMPLING_MAX_DEPTH + 1];
    private final int[] maxBranches = new int[SAMPLING_MAX_DEPTH + 1];
    private final int[] cacheHits = new int[SAMPLING_MAX_DEPTH + 1];
    private int totalEvaluated;
    private int totalCacheHits;
    private int totalReevaluations;

    // Map board state hash to cached AI calculations
    private final Map<UUID, DeterministicBoardStateNode> nodeMap;

    // When traversing the decision tree, keep track of which nodes we have already traversed, to avoid cycles
    // highly unlikely that we need this but just to be complete
    private final Set<DeterministicBoardStateNode> traversedNodes;

    // for big board swings, the AI should emote
    private int lastPlayerTurn;
    private double lastPlayerStartScore;

    AIConfig config;
    ServerBoard b;
    final DataStream dslocal;
    List<String> actionSendQueue;
    boolean waitForEvents, finishedTurn, sentGG;

    public AI(DataStream dslocal, AIConfig config) {
        this.config = config;
        this.dslocal = dslocal;
        this.b = new ServerBoard(0);
        this.b.logEvents = false;
        this.actionSendQueue = new LinkedList<>();
        this.nodeMap = new HashMap<>();
        this.traversedNodes = new HashSet<>();
    }

    @Override
    public void run() {
        try {
            while (this.b.getWinner() == 0 && !this.isInterrupted()) {
                this.readDataStream();
                Player localPlayer = this.b.getPlayer(this.b.getLocalteam());
                if (!this.isInterrupted() && !this.waitForEvents) {
                    if (!localPlayer.mulliganed && !localPlayer.getHand().isEmpty()) {
                        // do mulligan thing
                        List<Card> mulliganChoices = this.chooseMulligan();
                        this.dslocal.sendPlayerAction(new MulliganAction(localPlayer, mulliganChoices).toString());
                        this.waitForEvents = true;
                    } else if (this.b.getWinner() != 0 && !this.sentGG) {
                        this.sentGG = true;
                        if (this.b.getWinner() == this.b.getLocalteam()) {
                            if (this.lastPlayerTurn == this.b.getLocalteam() && this.lastPlayerStartScore < 0) {
                                // if lethal from comeback
                                this.dslocal.sendEmote(Math.random() > 0.5 ? Emote.THREATEN : Emote.WELLPLAYED);
                            } else {
                                this.dslocal.sendEmote(Emote.THREATEN);
                            }
                        } else {
                            if (this.lastPlayerTurn == this.b.getLocalteam() * -1 && this.lastPlayerStartScore > 0) {
                                // if lethal from comeback
                                this.dslocal.sendEmote(Math.random() > 0.5 ? Emote.SHOCKED : Emote.WELLPLAYED);
                            } else {
                                this.dslocal.sendEmote(Emote.WELLPLAYED);
                            }
                        }
                    } else if (this.b.getCurrentPlayerTurn() != this.lastPlayerTurn) {
                        double currentScore = evaluateAdvantage(this.b, this.b.getLocalteam());
                        double diff = EMOTE_SCORE_CHANGE_THRESHOLD + EMOTE_SCORE_CHANGE_THRESHOLD_PER_TURN * this.b.getPlayer(this.lastPlayerTurn).turn;
                        // if comeback
                        if (this.lastPlayerTurn == this.b.getLocalteam() && currentScore > 0 && lastPlayerStartScore < 0
                                && currentScore > this.lastPlayerStartScore + diff) {
                            // taunt them
                            this.dslocal.sendEmote(Emote.THREATEN);
                        } else if (this.lastPlayerTurn == this.b.getLocalteam() * -1 && currentScore < 0 && lastPlayerStartScore > 0
                                && currentScore < this.lastPlayerStartScore - diff) {
                            // wow well played
                            this.dslocal.sendEmote(Math.random() > 0.5 ? Emote.SHOCKED : Emote.WELLPLAYED);
                        }
                        this.lastPlayerTurn = this.b.getCurrentPlayerTurn();
                        this.lastPlayerStartScore = currentScore;
                    }
                    if (this.b.getCurrentPlayerTurn() == this.b.getLocalteam() && !this.finishedTurn) {
                        if (this.actionSendQueue.isEmpty() && this.b.getWinner() == 0) {
                            this.AIThink();
                        }
                        this.sendNextAction();
                        this.waitForEvents = true;
                    }
                }
            }
        } catch (IOException e) {
            // lol;
        }
    }

    private void readDataStream() throws IOException {
        MessageType mtype = this.dslocal.receive();
        switch (mtype) {
            case EVENT -> {
                List<EventBurst> eventBursts = this.dslocal.readEventBursts();
                this.b.consumeEventBursts(eventBursts);
                this.waitForEvents = false;
                if (this.b.getCurrentPlayerTurn() == this.b.getLocalteam()) {
                    this.finishedTurn = false;
                }
            }
            case COMMAND -> {
                String command = this.dslocal.readCommand();
                if (command.equals("reset")) {
                    this.b = new ServerBoard(this.b.getLocalteam());
                    this.b.logEvents = false;
                    this.actionSendQueue = new LinkedList<>();
                    this.waitForEvents = true;
                }
            }
            case TEAMASSIGN -> this.b.setLocalteam(this.dslocal.readTeamAssign());
            default -> {
                this.dslocal.discardMessage();
            }
        }
    }

    private List<Card> chooseMulligan() {
        // mulligan away cards that cost more than 3, unless they have spellboost
        return this.b.getPlayer(this.b.getLocalteam()).getHand().stream()
                .filter(c -> c.finalStats.get(Stat.COST) > 3 && c.finalStats.get(Stat.SPELLBOOSTABLE) == 0)
                .collect(Collectors.toList());
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
        DeterministicBoardStateNode dbsn = this.getBestTurn(this.b.getLocalteam(), 0, this.config.startSampleRate, Double.POSITIVE_INFINITY, false, null, null);
        if (dbsn == null) {
            System.out.println("AIThink returned from a null best turn, sadge");
            return;
        }
        double time = (System.nanoTime() - start) / 1000000000.;
        DeterministicBoardStateNode temp = dbsn;
        while (temp.team == this.b.getLocalteam() && (temp.isFullyEvaluated() || temp.lethal)) {
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
            System.out.println("Start score: " + evaluateAdvantage(this.b, this.b.getLocalteam()));
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

            // System.out.println("Nodes Traversed---");
            // temp = dbsn;
            // while (true) {
            //     String nextAction = temp.getMax().action;
            //     System.out.println(temp.toString());
            //     BoardStateNode next = temp.branches.get(nextAction);
            //     if (next instanceof DeterministicBoardStateNode) {
            //         temp = (DeterministicBoardStateNode) next;
            //     } else {
            //         break;
            //     }
            // }
            System.out.println("------------------");
        }
        this.actionSendQueue.addAll(actionStack);
    }

    private void sendNextAction() throws IOException {
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
     * @param state        If a state string was already calculated, put er here
     * @param stateHash    If a state hash was already calculated, put er here
     * @return A BoardStateNode object that has been populated, with data such as
     *         maxScore and maxAction, for the current board state
     */
    private DeterministicBoardStateNode getBestTurn(int team, int depth, double sampleRate, double maxSamples,
            boolean filterLethal, String state, UUID stateHash) {
        // no turn possible
        if (this.b.getCurrentPlayerTurn() != team || this.b.getWinner() != 0) {
            return null;
        }
        if (state == null) {
            state = this.b.stateToString();
            stateHash = UUID.nameUUIDFromBytes(state.getBytes());
        }
        DeterministicBoardStateNode dbsn;
        boolean cacheHit = false;
        if (!this.nodeMap.containsKey(stateHash)) {
            dbsn = new DeterministicBoardStateNode(team,
                    evaluateAdvantage(this.b, team),
                    filterLethal ? this.getPossibleLethalActions(team) : this.getPossibleActions(team));
            dbsn.sampleRate = sampleRate;
            this.nodeMap.put(stateHash, dbsn);
        } else {
            dbsn = this.nodeMap.get(stateHash);
            cacheHit = true;
            if (this.traversedNodes.contains(dbsn)) {
                System.err.println("AI encountered a cycle somehow");
                return null;
            }
        }
        this.traversedNodes.add(dbsn);
        // start sampling
        int numSamples = (int) Math.round(Math.min(dbsn.totalBranches * sampleRate, maxSamples));
        numSamples = Math.max(numSamples, SAMPLING_MIN_SAMPLES);
        numSamples = Math.min(numSamples, dbsn.totalBranches);
        // if we are revisiting this node, but it needs to be re-evaluated at a better detail level
        if (cacheHit && dbsn.sampleRate < sampleRate - this.config.reevaluationMaxSampleRateDiff) {
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
            String action = dbsn.nextBranchToEvaluate();
            TraversalResult traverse = this.traverseAction(dbsn, state, action, depth, sampleRate, maxSamples,
                    filterLethal, null);
            if (traverse.rng) {
                // rng, re-evaluate action many times, channel an average score into an RNGBoardStateNode
                int trials = Math.max(SelectRandom.ditherRound(Math.pow(this.config.rngDensityMultiplier, dbsn.totalBranches) * (this.config.rngMaxTrials - depth * this.config.rngTrialReduction)), this.config.rngMinTrials);
                BoardStateNode nextBsn = dbsn.branches.get(action);
                if (nextBsn != null && !(nextBsn instanceof RNGBoardStateNode)) {
                    // usually means you have a resolver with RNG but wasn't flagged appropriately
                    System.err.println("Expected a RNG node, instead found: ");
                    System.err.println(nextBsn.toString());
                    System.err.println("From action: " + action);
                    throw new IllegalStateException("rng");
                }
                RNGBoardStateNode nextRNGBSN = (RNGBoardStateNode) nextBsn; // trust
                if (nextRNGBSN != null) {
                    // if previously evaluated this rng branch, see if we can add some trials to it
                    trials = Math.max(1, trials - nextRNGBSN.trials);
                } else {
                    nextRNGBSN = new RNGBoardStateNode(team);
                    nextRNGBSN.addTrial(traverse.next);
                }
                for (int j = 1; j < trials; j++) {
                    traverse = this.traverseAction(dbsn, state, action, depth, sampleRate, maxSamples, filterLethal, nextRNGBSN);
                    nextRNGBSN.addTrial(traverse.next);
                }
                dbsn.logEvaluation(action, nextRNGBSN);
            } else {
                // not rng, whatever
                dbsn.logEvaluation(action, traverse.next);
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
     * @param currentState The current board state string (minor optimization for debugging)
     * @param action       The action to perform
     * @param depth        The current traversal depth
     * @param sampleRate   The proportion of total actions we sampled
     * @param maxSamples   The max number of samples we used before
     * @param filterLethal Flag to only traverse actions that may result in lethal
     * @param nextRNG      If we know the action results in rng, this is the rng node seen by the tree, for reference
     * @return The BoardStateNode at the end of the edge traversed
     */
    private TraversalResult traverseAction(DeterministicBoardStateNode current, String currentState, String action, int depth, double sampleRate, double maxSamples,
            boolean filterLethal, RNGBoardStateNode nextRNG) {
        ResolutionResult result = this.b.executePlayerAction(new StringTokenizer(action));
        BoardStateNode node;
        double nextMaxSamples = maxSamples;
        double nextSampleRate = sampleRate;
        if (depth + 1 >= SAMPLING_MIN_DEPTH) {
            if (depth + 1 == SAMPLING_MIN_DEPTH) {
                nextMaxSamples = this.config.maxSamples;
            } else {
                nextMaxSamples = maxSamples * this.config.maxSamplesMultiplier;
            }
            nextSampleRate = sampleRate * current.getWeightedProportionOfAction(action) * this.config.sampleRateMultiplier;
        }
        String state = null;
        UUID stateHash = null;
        if (result.rng) {
            // the number of trials we've already done to get to this node
            int cumTrials = 0;
            if (nextRNG != null) {
                state = this.b.stateToString();
                stateHash = UUID.nameUUIDFromBytes(state.getBytes());
                cumTrials += nextRNG.getCount(this.nodeMap.get(stateHash));
            }
            nextSampleRate *= 1 - (this.config.rngPenalty * Math.pow(this.config.rngPenaltyReduction, cumTrials));
        }
        if (this.b.getWinner() != 0 || depth == SAMPLING_MAX_DEPTH) {
            // no more actions can be taken
            node = new TerminalBoardStateNode(current.team, evaluateAdvantage(this.b, current.team));
            if (!result.rng && this.b.getWinner() == current.team) {
                current.lethal = true; // the current node has an action that results in a lethal 100% of the time
            }
        } else if (Integer.parseInt(action.substring(0, 1)) == EndTurnAction.ID || this.b.getCurrentPlayerTurn() != current.team) {
            // assess what's the worst the opponent can do
            if (filterLethal) {
                // not part of my pay grade
                node = new TerminalBoardStateNode(current.team * -1, evaluateAdvantage(this.b, current.team * -1));
            } else {
                node = this.getBestTurn(current.team * -1, depth + 1,
                        Math.max(nextSampleRate, this.config.enemySampleRate),
                        Math.max(nextMaxSamples, this.config.maxSamplesEnemy),
                        true, state, stateHash);
            }
        } else {
            DeterministicBoardStateNode dbsn = this.getBestTurn(current.team, depth + 1, nextSampleRate, nextMaxSamples, filterLethal, state, stateHash);
            if (dbsn == null) {
                // something went wrong, here's a bandaid
                node = new TerminalBoardStateNode(current.team, evaluateAdvantage(this.b, current.team));
            } else {
                if (dbsn.lethal) {
                    current.lethal = true; // the current node has an action that leads to a node that eventually results in lethal
                }
                node = dbsn;
            }
        }
        List<Event> undoStack = new LinkedList<>(result.events);
        while (!undoStack.isEmpty()) {
            undoStack.get(undoStack.size() - 1).undo(this.b);
            undoStack.remove(undoStack.size() - 1);
        }
        String stateAfterUndo = this.b.stateToString();
        if (!currentState.equals(stateAfterUndo)) {
            System.out.println(
                    "Discrepancy after executing " + action + ", rng = " + result.rng + ", depth = " + depth);
            for (Event e : result.events) {
                System.out.print(e.toString());
            }
            System.out.println("Before:");
            System.out.println(currentState);
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
        if (this.b.getCurrentPlayerTurn() != team || this.b.getWinner() != 0) {
            return null;
        }
        Player p = this.b.getPlayer(team);
        WeightedSampler<String> poss = new WeightedRandomSampler<>();
        // playing cards & selecting targets
        List<Card> hand = p.getHand();
        for (Card c : hand) {
            if (p.canPlayCard(c)) {
                double totalWeight = PLAY_CARD_TOTAL_WEIGHT + PLAY_CARD_COST_WEIGHT_MULTIPLIER * c.finalStats.get(Stat.COST);
                List<List<List<TargetList<?>>>> targetSearchSpace = new LinkedList<>();
                this.getPossibleListTargets(c.getBattlecryTargetingSchemes(), new LinkedList<>(), targetSearchSpace);
                if (targetSearchSpace.isEmpty()) {
                    targetSearchSpace.add(List.of());
                }
                if (c instanceof BoardObject) {
                    double weightPerPos = totalWeight / (p.getPlayArea().size() + 1);
                    // rip my branching factor lol
                    for (int playPos = 0; playPos <= p.getPlayArea().size(); playPos++) {
                        for (List<List<TargetList<?>>> targets : targetSearchSpace) {
                            poss.add(new PlayCardAction(p, c, playPos, targets).toString().intern(), weightPerPos / targetSearchSpace.size());
                        }
                    }
                } else {
                    // spells don't require positioning
                    for (List<List<TargetList<?>>> targets : targetSearchSpace) {
                        poss.add(new PlayCardAction(p, c, 0, targets).toString().intern(), totalWeight / targetSearchSpace.size());
                    }
                }
            }
        }
        this.b.getMinions(team, false, true).forEachOrdered(m -> {
            // unleashing cards & selecting targets
            if (p.canUnleashCard(m)) {
                List<List<List<TargetList<?>>>> targetSearchSpace = new LinkedList<>();
                this.getPossibleListTargets(m.getUnleashTargetingSchemes(), new LinkedList<>(), targetSearchSpace);
                if (targetSearchSpace.isEmpty()) {
                    targetSearchSpace.add(List.of());
                }
                for (List<List<TargetList<?>>> targets : targetSearchSpace) {
                    poss.add(new UnleashMinionAction(p, m, targets).toString().intern(), UNLEASH_TOTAL_WEIGHT / targetSearchSpace.size());
                }
            }
            // minion attack
            if (m.canAttack()) {
                double totalWeight = ATTACK_TOTAL_WEIGHT + ATTACK_WEIGHT_MULTIPLIER * m.finalStats.get(Stat.ATTACK);
                double weight = totalWeight / m.getAttackableTargets().count();
                m.getAttackableTargets().forEachOrdered(target -> {
                    double bonus = target instanceof Leader ? ATTACK_TARGET_LEADER_MULTIPLIER : 1;
                    double overkillMultiplier = Math.pow(ATTACK_WEIGHT_OVERKILL_PENALTY, Math.max(0, m.finalStats.get(Stat.ATTACK) - target.health));
                    poss.add(new OrderAttackAction(m, target).toString().intern(), overkillMultiplier * bonus * weight);
                });
            }
        });
        // ending turn
        poss.add(new EndTurnAction(team).toString().intern(), END_TURN_WEIGHT);
        return poss;
    }

    /**
     * Given the current board state, return the obvious actions that could lead to
     * a lethal. Intended to be a subset of all possible actions that are openly
     * visible on the board, i.e. unleashing minions and attacking face. Unlike
     * the more complete counterpart, this will not use random sampling; we
     * won't really get a chance to resample, and instead we prefer consistent
     * results, so upstream decision making is more consistent.
     * 
     * @param team The team to evaluate for
     * @return Some weighted set of actions that the team could take that could lead to lethal, to be sampled
     */
    private WeightedSampler<String> getPossibleLethalActions(int team) {
        if (this.b.getCurrentPlayerTurn() != team || this.b.getWinner() != 0) {
            return null;
        }
        Player p = this.b.getPlayer(team);
        WeightedSampler<String> poss = new WeightedOrderedSampler<>();
        List<Minion> minions = this.b.getMinions(team, false, true).collect(Collectors.toList());
        for (Minion m : minions) {
            if (p.canUnleashCard(m)) {
                double totalWeight = UNLEASH_TOTAL_WEIGHT + UNLEASH_WEIGHT_PER_PRESENCE * m.getTotalEffectValueOf(e -> e.getPresenceValue(5));
                List<List<List<TargetList<?>>>> targetSearchSpace = new LinkedList<>();
                this.getPossibleListTargets(m.getUnleashTargetingSchemes(), new LinkedList<>(), targetSearchSpace);
                if (targetSearchSpace.isEmpty()) {
                    targetSearchSpace.add(List.of());
                }
                for (List<List<TargetList<?>>> targets : targetSearchSpace) {
                    poss.add(new UnleashMinionAction(p, m, targets).toString().intern(), totalWeight / targetSearchSpace.size());
                }
            }
        }
        if (this.b.getMinions(team * -1, false, true).anyMatch(m -> m.finalStats.get(Stat.WARD) > 0)) {
            // find ways to break through the wards
            for (Minion m : minions) {
                if (m.canAttack()) {
                    double totalWeight = ATTACK_TOTAL_WEIGHT + ATTACK_WEIGHT_MULTIPLIER * m.finalStats.get(Stat.ATTACK);
                    List<Minion> searchSpace = m.getAttackableTargets().collect(Collectors.toList());
                    double weight = totalWeight / searchSpace.size();
                    for (Minion target : searchSpace) {
                        double overkillMultiplier = Math.pow(ATTACK_WEIGHT_OVERKILL_PENALTY, Math.max(0, m.finalStats.get(Stat.ATTACK) - target.health));
                        poss.add(new OrderAttackAction(m, target).toString().intern(), overkillMultiplier * weight);
                    }
                }
            }
        } else {
            // just go face
            this.b.getPlayer(team * -1).getLeader().ifPresent(l -> {
                for (Minion m : minions) {
                    if (m.canAttack(l)) {
                        double totalWeight = ATTACK_TOTAL_WEIGHT + ATTACK_WEIGHT_MULTIPLIER * m.finalStats.get(Stat.ATTACK);
                        poss.add(new OrderAttackAction(m, l).toString().intern(), totalWeight);
                    }
                }
            });
        }
        poss.add(new EndTurnAction(team).toString().intern(), END_TURN_WEIGHT);
        return poss;
    }

    /**
     * Given the current board state and a targeting requirement to fulfill,
     * recursively finds every possible combination of targets, returns null if it
     * cannot fully target because startInd was too large
     * 
     * @param t           The target schema to follow
     * @param current     Currently selected cards, for recursive purposes
     * @param searchSpace the list of cards to search through that are possible
     *                    targets, for optimization purposes, usually it's just
     *                    this.b.getTargetableCards(t)
     * @param startInd    the first index of targets in the searchspace that haven't
     *                    been considered, for optimization purposes, usually it's
     *                    just 0
     * @param outputList  List to output results to, for optimization purposes
     */
    private <T> void getPossibleTargets(TargetingScheme<T> t, TargetList<T> current, List<? extends T> searchSpace, int startInd,
                                        List<TargetList<T>> outputList) {
        if (t.isFullyTargeted(current)) {
            outputList.add(current);
            return;
        }
        if (startInd >= searchSpace.size()) {
            System.out.println("this shouldn't happen lmao");
        }
        int numToSelect = Math.min(searchSpace.size(), t.getMaxTargets());
        for (int i = startInd; i < searchSpace.size() - (numToSelect - current.targeted.size() - 1); i++) {
            T c = searchSpace.get(i);
            if (!current.targeted.contains(c)) {
                TargetList<T> copy = current.clone();
                copy.targeted.add(c);
                this.getPossibleTargets(t, copy, searchSpace, i + 1, outputList);
            }
        }
    }

    /**
     * Given the targeting schemes for 1 effect, conjure up all the possible
     * ways to target with that scheme. Assumes that order of targeting doesn't
     * matter.
     *
     * @param list The list of targets to fill
     * @param alreadyTargeted List of targets selected by previous schemes in
     *                        the same effect, for recursion purposes and
     *                        determines if the target is applicable
     * @param outputList List to populate with results, for optimization purposes
     *                   Each element is a way to assign targets for one
     *                   effect's targeting schemes
     */
    @SuppressWarnings("unchecked")
    private void getPossibleListTargetsPerEffect(List<TargetingScheme<?>> list, List<TargetList<?>> alreadyTargeted, List<List<TargetList<?>>> outputList) {
        if (list.isEmpty()) {
            outputList.add(new ArrayList<>(alreadyTargeted));
            return;
        }
        TargetingScheme<Object> scheme = (TargetingScheme<Object>) list.get(0);
        List<TargetList<Object>> searchSpace = new LinkedList<>();
        if (scheme.isApplicable(alreadyTargeted)) {
            this.getPossibleTargets(scheme, scheme.makeList(), scheme.getPossibleChoices(), 0, searchSpace);
        }
        if (searchSpace.isEmpty()) {
            searchSpace.add(scheme.makeList());
        }
        for (TargetList<?> t : searchSpace) {
            alreadyTargeted.add(t);
            this.getPossibleListTargetsPerEffect(list.subList(1, list.size()), alreadyTargeted, outputList);
            alreadyTargeted.remove(alreadyTargeted.size() - 1);
        }
    }

    /**
     * Given the current board state and a list of targets, recursively finds
     * every possible combination of target combos, e.g. if a card has 3
     * battlecry effects each with their own targeting scheme, this will find
     * every possible combination of targets of those 3 effects, like (0, 1, 2),
     * (0, 1, 3), (0, 2, 1), etc.
     *
     * @param list The list of targets to fill
     * @param alreadyTargeted List of already filled targets for recursion purposes
     * @param outputList List to populate with results, for optimization
     *                   purposes, each element in the list is a way to set
     *                   targets for a card
     */
    private void getPossibleListTargets(List<List<TargetingScheme<?>>> list, List<List<TargetList<?>>> alreadyTargeted, List<List<List<TargetList<?>>>> outputList) {
        // and the lord said let there be triple nested lists, trust me bro
        if (list.isEmpty()) {
            outputList.add(new ArrayList<>(alreadyTargeted));
            return;
        }
        List<TargetingScheme<?>> schemesForFirstEffect = list.get(0);
        List<List<TargetList<?>>> searchSpace = new LinkedList<>();
        this.getPossibleListTargetsPerEffect(schemesForFirstEffect, new LinkedList<>(), searchSpace);
        if (searchSpace.isEmpty()) {
            // if no targets, add that possibility to our search space
            searchSpace.add(List.of());
        }
        for (List<TargetList<?>> t : searchSpace) {
            alreadyTargeted.add(t);
            this.getPossibleListTargets(list.subList(1, list.size()), alreadyTargeted, outputList);
            alreadyTargeted.remove(alreadyTargeted.size() - 1);
        }
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
        return evaluateVictory(b, team) + evaluateMana(b, team)
                + evaluateSurvivability(b, team) + evaluateBoard(b, team) + evaluateHand(b, team)
                + evaluateDeck(b, team) + evaluateUnleashPower(b, team)
                - evaluateSurvivability(b, team * -1) - evaluateBoard(b, team * -1) - evaluateHand(b, team * -1)
                - evaluateDeck(b, team * -1) - evaluateUnleashPower(b, team * -1);
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
        if (b.getWinner() == team) {
            return 999999.;
        } else if (b.getWinner() == team * -1) {
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
        Optional<Leader> ol = b.getPlayer(team).getLeader();
        if (ol.isEmpty()) {
            return 0;
        }
        Leader l = ol.get();
        if (l.health <= 0) { // if dead
            return -99999 + l.health; // u dont want to be dead
        }
        int shield = l.finalStats.get(Stat.SHIELD);
        int armor = l.finalStats.get(Stat.ARMOR);
        Supplier<Stream<Minion>> attackingMinions = () -> b.getMinions(team * -1,  true, true);
        // TODO add if can attack check
        // TODO factor in damage limiting effects like durandal
        // this must be what the ppl who designed the tax code feel like
        int potentialDamage = attackingMinions.get()
                .filter(Minion::canAttack)
                .map(m -> m.finalStats.get(Stat.ATTACK) * (m.finalStats.get(Stat.ATTACKS_PER_TURN) - m.attacksThisTurn))
                .reduce(0, Integer::sum);
        int potentialBypassWardDamage = attackingMinions.get()
                .filter(Minion::canAttack)
                .filter(m -> m.finalStats.get(Stat.IGNORE_WARD) > 0)
                .map(m -> m.finalStats.get(Stat.ATTACK) * (m.finalStats.get(Stat.ATTACKS_PER_TURN) - m.attacksThisTurn))
                .reduce(0, Integer::sum);
        int potentialLeaderDamage = attackingMinions.get()
                .filter(Minion::canAttack)
                .filter(Minion::attackLeaderConditions)
                .map(m -> Math.max(m.finalStats.get(Stat.ATTACK) - armor, 0) * (m.finalStats.get(Stat.ATTACKS_PER_TURN) - m.attacksThisTurn))
                .reduce(0, Integer::sum);
        int threatenDamage = attackingMinions.get()
                .filter(Minion::canAttackEventually)
                .map(m -> m.finalStats.get(Stat.ATTACK) * m.finalStats.get(Stat.ATTACKS_PER_TURN))
                .reduce(0, Integer::sum);
        int threatenBypassWardDamage = attackingMinions.get()
                .filter(Minion::canAttackEventually)
                .filter(m -> m.finalStats.get(Stat.IGNORE_WARD) > 0)
                .map(m -> m.finalStats.get(Stat.ATTACK) * m.finalStats.get(Stat.ATTACKS_PER_TURN))
                .reduce(0, Integer::sum);
        int threatenLeaderDamage = attackingMinions.get()
                .filter(Minion::canAttackEventually)
                .filter(Minion::attackLeaderConditions)
                .map(m -> Math.max(m.finalStats.get(Stat.ATTACK) - armor, 0) * m.finalStats.get(Stat.ATTACKS_PER_TURN))
                .reduce(0, Integer::sum);
        int attackers = attackingMinions.get()
                .map(m -> m.finalStats.get(Stat.ATTACKS_PER_TURN))
                .reduce(0, Integer::sum);
        List<Minion> defendingMinons = b.getMinions(team, false, true)
                .filter(m -> m.finalStats.get(Stat.WARD) > 0)
                .collect(Collectors.toList());
        int leaderhp = l.health + shield;
        int ehp = defendingMinons.stream()
                .map(m -> m.health)
                .reduce(leaderhp, Integer::sum);
        long defenders = defendingMinons.size();
        if (shield > 0) {
            defenders++;
        }
        if (b.getCurrentPlayerTurn() != team) {
            threatenDamage = potentialDamage;
            threatenLeaderDamage = potentialLeaderDamage;
            threatenBypassWardDamage = potentialBypassWardDamage;
        }
        // if there are more defenders than attacks, then minions shouldn't be
        // able to touch face
        int baseline = defenders >= attackers ?  leaderhp - threatenBypassWardDamage :  leaderhp - threatenLeaderDamage;
        ehp = Math.max(ehp - threatenDamage, baseline);
        if (ehp <= 0 || threatenBypassWardDamage >= leaderhp) {
            if (l.finalStats.get(Stat.UNYIELDING) > 0) {
                ehp = 1;
            } else if (team == b.getCurrentPlayerTurn()) {
                // they're threatening lethal if i dont do anything
                return -30 + ehp;
            } else {
                // they have lethal, i am ded
                return -60 + ehp;
            }
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
        for (BoardObject bo : b.getPlayer(team).getPlayArea()) {
            total += bo.getValue();
        }
        return total;
    }

    /**
     * More cards in hand is better, but you also want playable stuff. The power of
     * a card is value/(cost + 1.1), which usually comes to about 1 mana per
     * card. A more powerful hand would have higher value cards with lower cost.
     * The 1.1 is there to dissuade the AI from obsessively holding on to 0-cost
     * cards because there might be a time in the future where it can get better
     * value.
     * 
     * @param b    The board
     * @param team the team to evaluate for. can cheat by looking at opponents hand
     *             lol
     * @return the mana value of having these cards in hand
     */
    public static double evaluateHand(Board b, int team) {
        double totalPower = 0;
        for (Card c : b.getPlayer(team).getHand()) {
            totalPower += valueInHand(c);
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
        // assumes that team 1 starts
        if (b.getCurrentPlayerTurn() == 1) {
            // players went an uneven number of turns, counteract that so the
            // advantage value doesn't change crazily when players end their
            // turns, making the AI more stable
            if (team == 1) {
                enemymax = Math.min(enemymax + 1, enemymaxmax);
            } else {
                localmax = Math.min(localmax + 1, localmaxmax);
            }
        }
        return (localmax - enemymax) * Math.max(localmaxmax - localmax, enemymaxmax - enemymax);
    }

    /**
     * Puts a mana value for being closer to fatigue
     * With a healthy deck size, drawing cards doesn't matter, but as you get
     * closer to 0 cards in the deck, we start getting serious issues
     * TODO: reverse this if spartacus effect
     *
     * @param b    The board
     * @param team The team to evaluate for
     * @return Mana value of being this close to fatigue (a negative value)
     */
    public static double evaluateDeck(Board b, int team) {
        Player p = b.getPlayer(team);
        double avgValue = p.getDeck().isEmpty() ? 0 : p.getDeck().stream()
                .map(AI::valueInHand)
                .reduce(0., Double::sum) / p.getDeck().size();
        return -150 * Math.pow(p.getDeck().size() + 0.5, -1.5) + avgValue;
    }

    /**
     * Ascribe a mana value to having a better/worse unleash power
     * @param b The board
     * @param team The team to evaluate for
     * @return Mana value of having this unleash power
     */
    public static double evaluateUnleashPower(Board b, int team) {
        // primitive unleash power value
        if (!b.getPlayer(team).unleashAllowed) {
            return 0;
        }
        Optional<UnleashPower> oup = b.getPlayer(team).getUnleashPower();
        if (oup.isEmpty()) {
            return 0;
        }
        UnleashPower up = oup.get();
        return up.getValue() / (up.finalStats.get(Stat.COST) + 1);
    }

    /**
     * Helper func to determine the mana value of an effect that summons stuff
     * on the board
     * @param instances Instance of the cards to summon in order (probably cached)
     * @param refs Max depth of calculations when referencing other cards
     * @return The approximate mana value
     */
    public static double valueForSummoning(List<? extends Card> instances, int refs) {
        // behold magic numbers
        double sum = 0;
        double multiplier = 0.94;
        for (Card c : instances) {
            sum += c.getValue(refs - 1) * multiplier;
            multiplier *= multiplier; // each card has lower and lower chance of being able to fit
        }
        return sum;
    }

    /**
     * Helper func to determine the mana value of an effect that puts stuff in
     * your hand
     * @param instances Instance of the cards to add in order (probably cached)
     * @param refs Max depth of calculations when referencing other cards
     * @return The approximate mana value
     */
    public static double valueForAddingToHand(List<? extends Card> instances, int refs) {
        // behold magic numbers
        double sum = 0;
        double multiplier = 0.99;
        for (Card c : instances) {
            sum += valueInHand(c, refs - 1) * multiplier;
            multiplier *= multiplier; // each card has lower and lower chance of being able to fit
        }
        return sum;
    }

    /**
     * Helper func to determine the mana value of an effect that puts stuff in
     * your deck
     * @param instances Instances of the cards to add to deck
     * @param refs Max depth of calculations when referencing other cards
     * @return The approximate mana value
     */
    public static double valueForAddingToDeck(List<? extends Card> instances, int refs) {
        // some bullshit
        return instances.stream()
                .map(c -> valueInHand(c, refs - 1))
                .reduce(0., Double::sum) / 10;
    }

    /**
     * Helper func to determine the mana value of a stat buff
     * @param attack The attack buff
     * @param magic The magic buff
     * @param health The health buff
     * @return The approximate mana value
     */
    public static double valueForBuff(int attack, int magic, int health) {
        return attack * 0.5 + health * 0.5 + magic * 0.3;
    }

    /**
     * Gets the value of having a card in hand
     * @param c the card
     * @param refs the max depth of using other card's values in the value calculation
     * @return the value
     */
    public static double valueInHand(Card c, int refs) {
        double rushStormBonus = 0;
        if (c instanceof Minion) {
            if (c.finalStats.get(Stat.RUSH) > 0) {
                rushStormBonus = valueOfRush(c.finalStats.get(Stat.ATTACK));
            }
            if (c.finalStats.get(Stat.STORM) > 0) {
                rushStormBonus = valueOfStorm(c.finalStats.get(Stat.ATTACK));
            }
        }
        return (c.getValue(refs) + rushStormBonus) / (c.finalStats.get(Stat.COST) + 1.1);
    }

    /**
     * Gets the value of having a card in hand
     * @param c the card
     * @return the value
     */
    public static double valueInHand(Card c) {
        double rushStormBonus = 0;
        if (c instanceof Minion) {
            if (c.finalStats.get(Stat.RUSH) > 0) {
                rushStormBonus = valueOfRush(c);
            }
            if (c.finalStats.get(Stat.STORM) > 0) {
                rushStormBonus = valueOfStorm(c);
            }
        }
        return (c.getValue() + rushStormBonus) / (c.finalStats.get(Stat.COST) + 1.1);
    }

    /**
     * Gets the value of doing damage to a single minion
     * @param damage the damage dealt to a minion
     * @return the value
     */
    public static double valueOfMinionDamage(int damage) {
        return Math.min(AI.VALUE_OF_DESTROY, 2.466 * Math.log(damage / 2. + 1));
    }

    /**
     * Gets the value of granting rush to a minion
     * @param attack The attack of the minion
     * @return the value
     */
    public static double valueOfRush(int attack) {
        return valueOfMinionDamage(attack) * 0.5;
    }

    // same as above but automatically gets the attack stat
    public static double valueOfRush(Card c) {
        return valueOfRush(c.finalStats.get(Stat.ATTACK));
    }

    /**
     * Gets the value of granting storm to a minion
     * @param attack The attack of the minion
     * @return the value
     */
    public static double valueOfStorm(int attack) {
        return attack * 0.8;
    }

    // same as above but automatically gets the attack stat
    public static double valueOfStorm(Card c) {
        return valueOfStorm(c.finalStats.get(Stat.ATTACK));
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
