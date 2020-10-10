package server.ai;

import java.util.*;

import network.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.playeraction.*;

/**
 * This AI class is all about making decisions for a player in a game. The AI
 * outputs playeractions and receives updates about the board through a
 * DataStream.
 * 
 * @author Michael
 *
 */
public class AI extends Thread {
	private static final int MAX_RNG_TRIALS = 5;
	private static final int MIN_RNG_TRIALS = 2;

	/*
	 * We can't expect the AI to traverse every single possible node in the decision
	 * tree before making a move (especially considering rng), so after a certain
	 * depth we will only sample some of the decisions and extrapolate the strength
	 * of the overall turn from there. When the AI commits its decisions and reaches
	 * this depth again, we will have to re-evaluate which action is best.
	 */
	// The minimum depth for sampling to occur
	private static final int REEVALUATION_MIN_DEPTH = 1;

	// After this depth, just kinda call it
	private static final int REEVALUATION_MAX_DEPTH = 10;

	// The minimum number of branches to sample at each level
	private static final int REEVALUATION_MIN_SAMPLES = 1;

	// The maximum number of branches to sample at min depth
	private static final int REEVALUATION_MAX_SAMPLES = 16;

	// How much the max number of samples gets multiplied by per level
	private static final double REEVALUATION_MAX_SAMPLES_MULTIPLIER = 0.5;

	// Proportion of total possible actions that we sample at min depth
	private static final double REEVALUATION_SAMPLE_RATE = 1;

	// Multiplier of sample rate at each depth
	private static final double REEVALUATION_SAMPLE_RATE_MULTIPLIER = 0.5;

	// Statistics to gauge AI evaluation speed
	private int[] width = new int[REEVALUATION_MAX_DEPTH + 1], maxBranches = new int[REEVALUATION_MAX_DEPTH + 1],
			cacheHits = new int[REEVALUATION_MAX_DEPTH + 1];

	// Map board state to cached AI calculations
	private Map<String, BoardStateNode> nodeMap;

	int difficulty;
	Board b;
	DataStream dslocal;
	List<String> actionSendQueue;
	boolean waitForEvents, finishedTurn;

	public AI(DataStream dslocal, int team, int difficulty) {
		this.difficulty = difficulty;
		this.dslocal = dslocal;
		this.b = new Board(team);
		this.actionSendQueue = new LinkedList<String>();
		this.nodeMap = new HashMap<>();
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
				this.actionSendQueue = new LinkedList<String>();
				this.waitForEvents = true;
			default:
				break;
			}
		}
	}

	private void AIThink() {
		System.out.println("Start score: " + evaluateAdvantage(this.b, this.b.localteam));
		for (int i = 0; i < this.width.length; i++) {
			this.width[i] = 0;
			this.maxBranches[i] = 0;
			this.cacheHits[i] = 0;
		}
		List<String> actionStack = new LinkedList<>();
		long start = System.nanoTime();
		BoardStateNode bsn = this.getBestTurn(this.b.localteam, 0, 1, Double.POSITIVE_INFINITY, false);
		if (bsn == null) {
			System.out.println("AIThink returned from a null best turn, sadge");
			return;
		}
		double time = (System.nanoTime() - start) / 1000000000.;
		BoardStateNode temp = bsn;
		while (temp.definedNext && temp.team == this.b.localteam && (temp.isFullyEvaluated() || temp.lethal)) {
			String nextAction = temp.maxAction;
			actionStack.add(nextAction);
			temp = temp.branches.get(nextAction);
		}
		if (actionStack.isEmpty()) {
			System.out.println("AIThink produced no actions!");
		}
		temp = bsn;
		while (temp.definedNext) {
			String nextAction = temp.maxAction;
			System.out.println(temp.toString());
			temp = temp.branches.get(nextAction);
		}
		this.actionSendQueue.addAll(actionStack);
		System.out.println("Time taken: " + time);
		System.out.println("Score achieved: " + bsn.maxScore);
		System.out.printf("%-6s %-6s %-6s %-6s", "Depth", "Width", "Cache", "Brnchs\n");
		for (int i = 0; i < this.width.length; i++) {
			if (this.width[i] == 0) {
				break;
			}
			System.out.printf("%6d %6d %6d %6d\n", i, this.width[i], this.cacheHits[i], this.maxBranches[i]);
		}
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
	private BoardStateNode getBestTurn(int team, int depth, double sampleRate, double maxSamples,
			boolean filterLethal) {
		// no turn possible
		if (this.b.currentPlayerTurn != team || this.b.winner != 0) {
			return null;
		}
		String state = this.b.stateToString();
		BoardStateNode bsn;
		boolean cacheHit = false;
		if (!this.nodeMap.containsKey(state)) {
			bsn = new BoardStateNode(team,
					filterLethal ? this.getPossibleLethalActions(team) : this.getPossibleActions(team));
			this.nodeMap.put(state, bsn);
			bsn.currScore = evaluateAdvantage(this.b, team);
		} else {
			bsn = this.nodeMap.get(state);
			cacheHit = true;
		}
		// start sampling
		int numSamples;
		if (depth >= REEVALUATION_MIN_DEPTH) {
			numSamples = (int) Math.min(bsn.totalBranches * sampleRate, maxSamples);
			numSamples = Math.max(numSamples, REEVALUATION_MIN_SAMPLES);
			numSamples = Math.min(numSamples, bsn.totalBranches);
		} else {
			numSamples = bsn.totalBranches;
		}
		// some statistics for debugging purposes
		this.maxBranches[depth] = Math.max(this.maxBranches[depth], numSamples);
		this.width[depth]++;
		if (cacheHit) {
			this.cacheHits[depth]++;
		}
		for (int i = bsn.branches.size(); i < numSamples; i++) {
			int branchIndex = (int) (Math.random() * bsn.unevaluatedBranches.size());
			String actionString = bsn.unevaluatedBranches.get(branchIndex);
			List<Event> undoStack = new LinkedList<Event>();
			List<String> turn = new LinkedList<String>();
			turn.add(actionString);
			List<Effect> listeners = new ArrayList<>();
			listeners.addAll(this.b.getEventListeners());
			StringBuilder listenerStates = new StringBuilder();
			for (Effect e : listeners) {
				listenerStates.append(e.extraStateString());
			}
			List<Event> happenings = this.b.executePlayerAction(new StringTokenizer(actionString));
			boolean rng = false;
			for (Event e : happenings) {
				if (e.rng) {
					rng = true;
					break;
				}
			}
			BoardStateNode nextBsn = this.traverseAction(team, actionString, depth, sampleRate, maxSamples,
					filterLethal);
			boolean assuredLethal = (nextBsn.team == team && nextBsn.lethal) || (!rng && this.b.winner == team);
			undoStack.addAll(happenings);
			while (!undoStack.isEmpty()) {
				undoStack.get(undoStack.size() - 1).undo();
				undoStack.remove(undoStack.size() - 1);
			}
			StringTokenizer st = new StringTokenizer(listenerStates.toString());
			/*
			 * This here assumes that changes to the list of eventlisteners can be fully
			 * undone
			 */
			for (Effect e : listeners) {
				e.loadExtraState(this.b, st);
			}
			String stateAfter = this.b.stateToString();
			if (!state.equals(stateAfter)) {
				System.out.println(
						"Discrepancy after executing " + turn.get(0) + ", rng = " + rng + ", depth = " + depth);
				for (Event e : happenings) {
					System.out.print(e.toString());
				}
				System.out.println("Before:");
				System.out.println(state);
				System.out.println("After:");
				System.out.println(stateAfter);
			}
			if (rng) {
				// TODO: have AI re-evaluate rng events if the depth was too
				// high
				int trials = Math.max(MAX_RNG_TRIALS - depth, MIN_RNG_TRIALS);
				double score = nextBsn.maxScore * team * nextBsn.team;
				for (int j = 1; j < trials; j++) {
					listenerStates = new StringBuilder();
					for (Effect e : listeners) {
						listenerStates.append(e.extraStateString());
					}
					happenings = this.b.executePlayerAction(new StringTokenizer(actionString));
					// TODO: make easier difficulties not traverse the tree
					nextBsn = this.traverseAction(team, actionString, depth, sampleRate, maxSamples, filterLethal);
					score += nextBsn.maxScore * team * nextBsn.team;
					undoStack.addAll(happenings);
					while (!undoStack.isEmpty()) {
						undoStack.get(undoStack.size() - 1).undo();
						undoStack.remove(undoStack.size() - 1);
					}
					st = new StringTokenizer(listenerStates.toString());
					for (Effect e : listeners) {
						e.loadExtraState(this.b, st);
					}
					stateAfter = this.b.stateToString();
					if (!state.equals(stateAfter)) {
						System.out.println(
								"Discrepancy after executing " + turn.get(0) + ", rng = " + rng + ", depth = " + depth);
						for (Event e : happenings) {
							System.out.println(e.toString());
						}
						System.out.println("Before:");
						System.out.println(state);
						System.out.println("After:");
						System.out.println(stateAfter);
					}
				}
				score /= trials; // get the average result
				bsn.logEvaluation(branchIndex, new BoardStateNode(team, score));
			} else {
				bsn.logEvaluation(branchIndex, nextBsn);
			}

			if (assuredLethal) {
				bsn.lethal = true;
				break;
			}
		}

		return bsn;
	}

	/**
	 * Helper method (for getBestTurn) to perform a traversal of the decision tree,
	 * recursively exploring the subtree. Assumes that the board is in the
	 * post-traversal state, i.e. the action has been carried out already.
	 * 
	 * @param team         The team from which we consider
	 * @param action       The action to perform
	 * @param depth        The current traversal depth
	 * @param sampleRate   The proportion of total actions we sampled
	 * @param maxSamples   The max number of samples we used before
	 * @param filterLethal Flag to only traverse actions that may result in lethal
	 * @return The BoardStateNode at the end of the edge traversed
	 */
	private BoardStateNode traverseAction(int team, String action, int depth, double sampleRate, double maxSamples,
			boolean filterLethal) {
		BoardStateNode node;
		double nextMaxSamples = maxSamples;
		double nextSampleRate = sampleRate;
		if (depth + 1 == REEVALUATION_MIN_DEPTH) {
			nextMaxSamples = REEVALUATION_MAX_SAMPLES;
			nextSampleRate = REEVALUATION_SAMPLE_RATE;
		} else if (depth + 1 > REEVALUATION_MIN_DEPTH) {
			nextMaxSamples = maxSamples * REEVALUATION_MAX_SAMPLES_MULTIPLIER;
			nextSampleRate = sampleRate * REEVALUATION_SAMPLE_RATE_MULTIPLIER;
		}
		if (this.b.winner != 0 || depth == REEVALUATION_MAX_DEPTH) {
			// no more actions can be taken
			node = new BoardStateNode(team, evaluateAdvantage(this.b, team));
		} else if (Integer.parseInt(action.substring(0, 1)) == EndTurnAction.ID || this.b.currentPlayerTurn != team) {
			// assess what's the worst the opponent can do
			double score = evaluateAdvantage(this.b, team);
			if (filterLethal) {
				node = new BoardStateNode(team, score);
			} else {
				node = this.getBestTurn(team * -1, depth + 1, nextSampleRate, nextMaxSamples, true);
			}
		} else {
			node = this.getBestTurn(team, depth + 1, nextSampleRate, nextMaxSamples, filterLethal);
		}
		return node;
	}

	/**
	 * Given the current board state, attempts to find every possible action that a
	 * player on this team can make.
	 * 
	 * @param team The team to evaluate for
	 * @return a list of possible actions taken by the AI
	 */
	private List<String> getPossibleActions(int team) {
		if (this.b.currentPlayerTurn != team || this.b.winner != 0) {
			return null;
		}
		Player p = this.b.getPlayer(team);
		List<String> poss = new LinkedList<>();
		List<BoardObject> minions = this.b.getBoardObjects(team, false, true, false);
		for (BoardObject b : minions) {
			// minion attack
			Minion m = (Minion) b;
			if (m.canAttack()) {
				for (Minion target : m.getAttackableTargets()) {
					poss.add(new OrderAttackAction(m, target).toString());
				}
			}
			// unleashing cards & selecting targets
			if (p.canUnleashCard(m)) {
				if (!m.getUnleashTargets().isEmpty()) {
					List<List<Target>> targetSearchSpace = this.getPossibleListTargets(m.getUnleashTargets());
					for (List<Target> targets : targetSearchSpace) {
						poss.add(new UnleashMinionAction(p, m, Target.listToString(targets)).toString());
					}
				} else { // no targets to set
					poss.add(new UnleashMinionAction(p, m, "0").toString());
				}
			}
		}
		// playing cards & selecting targets
		List<Card> hand = p.hand.cards;
		for (Card c : hand) {
			// TODO make it consider board positioning
			if (p.canPlayCard(c)) {
				if (!c.getBattlecryTargets().isEmpty()) {
					List<List<Target>> targetSearchSpace = this.getPossibleListTargets(c.getBattlecryTargets());
					for (List<Target> targets : targetSearchSpace) {
						poss.add(new PlayCardAction(p, c, 0, Target.listToString(targets)).toString());
					}
				} else { // no targets to set
					poss.add(new PlayCardAction(p, c, 0, "0").toString());
				}
			}
		}
		// ending turn
		poss.add(new EndTurnAction(team).toString());
		return poss;
	}

	/**
	 * Given the current board state, return the obvious actions that could lead to
	 * a lethal. Intended to be a subset of all possible actions that are openly
	 * visible on the board, i.e. unleashing minions and attacking face.
	 * 
	 * @param team The team to evaluate for
	 * @return The set of actions that the team could take that could lead to lethal
	 */
	private List<String> getPossibleLethalActions(int team) {
		if (this.b.currentPlayerTurn != team || this.b.winner != 0) {
			return null;
		}
		Player p = this.b.getPlayer(team);
		List<String> poss = new LinkedList<>();
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
				if (!m.getUnleashTargets().isEmpty()) {
					List<List<Target>> targetSearchSpace = this.getPossibleListTargets(m.getUnleashTargets());
					for (List<Target> targets : targetSearchSpace) {
						poss.add(new UnleashMinionAction(p, m, Target.listToString(targets)).toString());
					}
				} else { // no targets to set
					poss.add(new UnleashMinionAction(p, m, "0").toString());
				}
			}
		}
		if (enemyWard) {
			// find ways to break through the wards
			for (Minion m : minions) {
				if (m.canAttack()) {
					for (Minion target : m.getAttackableTargets()) {
						poss.add(new OrderAttackAction(m, target).toString());
					}
				}
			}
		} else {
			// just go face
			Leader enemyFace = this.b.getPlayer(team * -1).leader;
			for (Minion m : minions) {
				if (m.canAttack(enemyFace)) {
					poss.add(new OrderAttackAction(m, enemyFace).toString());
				}
			}
		}
		poss.add(new EndTurnAction(team).toString());
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
		List<Target> poss = new LinkedList<Target>();
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
		List<List<Target>> poss = new LinkedList<List<Target>>();
		if (list.isEmpty()) {
			return poss;
		}
		list.get(0).reset();
		List<Target> searchspace = this.getPossibleTargets(list.get(0), this.b.getTargetableCards(list.get(0)), 0);
		for (Target t : searchspace) {
			List<Target> posscombo = new LinkedList<Target>();
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
			return 99999999999.;
		} else if (b.winner == team * -1) {
			return -99999999999.;
		} else {
			return 0;
		}
	}

	/**
	 * Health is a resource, attempts to evaluate the mana worth of having high
	 * hp/opponent at low hp, factoring in enemy minions and friendly wards. The
	 * formula is mana = 6ln(ehp), making a 12hp heal worth about 5 mana when at 9
	 * hp, equivalent to greater healing potion, and a 6hp nuke worth about 5 mana
	 * when opponent is at 11 hp, equivalent to fireball
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
			return -99999999 + l.health; // u dont want to be dead
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
		// if there are more defenders than attacks, then minions shouldn't be
		// able to touch face
		if (defenders >= attackers) {
			ehp = Math.max(ehp - threatenDamage, l.health);
		} else if (team == b.currentPlayerTurn) {
			if (threatenDamage >= ehp) {
				// they're threatening lethal if i dont do anything
				return -999999 + ehp - threatenDamage;
			}
			ehp -= threatenDamage;
		} else {
			if (potentialDamage >= ehp) {
				// they have lethal. i am ded
				return -9999999 + ehp - potentialDamage;
			}
			if (threatenDamage >= ehp) {
				// gee their board is a bit scary
				return -99999 + ehp - threatenDamage;
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
		return b.getPlayer(team).maxmana - b.getPlayer(-team).maxmana;
	}
}
