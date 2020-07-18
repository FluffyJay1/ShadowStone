package server;

import java.util.*;

import client.*;
import network.*;
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
	private static final int MAX_RNG_TRIALS = 4;
	private static final int MIN_RNG_TRIALS = 2;

	/*
	 * We can't expect the AI to traverse every single possible node in the decision
	 * tree before making a move (especially considering rng), so after a certain
	 * depth we will only sample some of the decisions and extrapolate the strength
	 * of the overall turn from there. When the AI commits its decisions and reaches
	 * this depth again, we will have to re-evaluate which action is best.
	 */
	// The minimum depth for sampling to occur
	private static final int REEVALUATION_MIN_DEPTH = 2;

	// After this depth, just kinda call it
	private static final int REEVALUATION_MAX_DEPTH = 6;

	// At min depth, the fraction of total actions to actually sample
	private static final double REEVALUATION_SAMPLE_RATE = 0.5;

	// For each node past minimum depth, the sample rate is decreased
	private static final double REEVALUATION_SAMPLE_RATE_MULTIPLIER = 0.5;

	// The minimum number of branches to sample at each level
	private static final int REEVALUATION_MIN_SAMPLES = 1;

	// The maximum number of branches to sample at each level
	private static final int REEVALUATION_MAX_SAMPLES = 10;

	/*
	 * Past a certain depth, the sampling rate is probably bad, so let's also assume
	 * that whatever actions the AI takes, it won't make the board state worse than
	 * it already is
	 */
	private static final int REEVALUATION_ESTIMATION_DEPTH = 4;

	// Statistics to gauge AI evaluation speed
	private int[] width = new int[50], maxBranches = new int[50];

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
	}

	@Override
	public void run() {
		while (this.b.winner == 0) {
			this.readDataStream();
			if (this.b.currentPlayerTurn == this.b.localteam && !this.finishedTurn && !this.waitForEvents) {
				if (this.actionSendQueue.isEmpty()) {
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
		}
		List<String> actionStack = new LinkedList<String>();
		long start = System.nanoTime();
		double score = this.getBestTurn(actionStack, 0, 1);
		double time = (System.nanoTime() - start) / 1000000000.;
		this.actionSendQueue.addAll(actionStack);
		System.out.println("Time taken: " + time);
		System.out.println("Score achieved: " + score);
		System.out.printf("%-6s %-6s %-12s", "Depth", "Width", "Max Branches\n");
		for (int i = 0; i < this.width.length; i++) {
			if (this.width[i] == 0) {
				break;
			}
			System.out.printf("%6d %6d %12d\n", i, this.width[i], this.maxBranches[i]);
		}
	}

	private void sendNextAction() {
		// TODO: check if action is still valid
		String action = this.actionSendQueue.remove(0);
		this.dslocal.sendPlayerAction(action);
		StringTokenizer st = new StringTokenizer(action);
		if (Integer.parseInt(st.nextToken()) == EndTurnAction.ID) {
			this.finishedTurn = true;
		}
	}

	/**
	 * Given the current board state, get the sequence of player actions that
	 * maximizes the advantage of the AI.
	 * 
	 * @param actions    A list that will contain the sequence of player actions
	 *                   after the method is run, can be null if not necessary
	 * @param depth      Passes the current depth in the decision tree, should start
	 *                   at 0
	 * @param sampleRate The fraction of total possible actions that we sample
	 * @return The best advantage score
	 */
	private double getBestTurn(List<String> actions, int depth, double sampleRate) {
		// TODO: implement simplified version of this method for lethal checking, used
		// by AI to make sure oppponent doesn't have lethal
		List<PlayerAction> poss = this.getPossibleActions(), samples;
		List<String> bestTurn = new LinkedList<String>();
		double bestScore = Double.NEGATIVE_INFINITY;
		// start sampling
		if (depth >= REEVALUATION_MIN_DEPTH) {
			int numSamples = (int) Math.max(poss.size() * sampleRate, REEVALUATION_MIN_SAMPLES);
			numSamples = Math.min(numSamples, REEVALUATION_MAX_SAMPLES);
			samples = Game.selectRandom(poss, numSamples);
		} else {
			samples = poss;
		}
		// some statistics for debugging purposes
		this.maxBranches[depth] = Math.max(this.maxBranches[depth], samples.size());
		this.width[depth]++;
		for (PlayerAction action : samples) {
			String stateBefore = this.b.stateToString();
			List<Event> undoStack = new LinkedList<Event>();
			List<String> turn = new LinkedList<String>();
			turn.add(action.toString());
			List<Event> happenings = this.b.executePlayerAction(new StringTokenizer(action.toString()));
			boolean rng = false;
			for (Event e : happenings) {
				if (e.rng) {
					rng = true;
					break;
				}
			}
			boolean assuredLethal = !rng && this.b.winner == this.b.localteam;
			double score = 0;
			// If we are uncertain about actions after this, don't commit those actions.
			// Don't keep track of the best actions after this.
			boolean uncertain = rng || samples.size() < poss.size();
			score = this.traverseAction(action, uncertain ? null : turn, depth, sampleRate);
			undoStack.addAll(happenings);
			while (!undoStack.isEmpty()) {
				undoStack.get(undoStack.size() - 1).undo();
				undoStack.remove(undoStack.size() - 1);
			}
			String stateAfter = this.b.stateToString();
			if (!stateBefore.equals(stateAfter)) {
				System.out.println(
						"Discrepancy after executing " + turn.get(0) + ", rng = " + rng + ", depth = " + depth);
				for (Event e : happenings) {
					System.out.println(e.toString());
				}
				System.out.println("Before:");
				System.out.println(stateBefore);
				System.out.println("After:");
				System.out.println(stateAfter);
			}

			if (rng) {
				// TODO: have AI re-evaluate rng events if the depth was too
				// high
				int trials = Math.max(MAX_RNG_TRIALS - depth, MIN_RNG_TRIALS);
				for (int i = 1; i < trials; i++) {
					happenings = this.b.executePlayerAction(new StringTokenizer(action.toString()));
					// TODO: make easier difficulties not traverse the tree
					score += this.traverseAction(action, null, depth, sampleRate);
					undoStack.addAll(happenings);
					while (!undoStack.isEmpty()) {
						undoStack.get(undoStack.size() - 1).undo();
						undoStack.remove(undoStack.size() - 1);
					}
				}
				score /= trials; // get the average result

			}

			if (score > bestScore || assuredLethal) {
				bestTurn = turn;
				bestScore = score;
			}

			if (assuredLethal) {
				break;
			}
		}
		if (actions != null) {
			actions.addAll(bestTurn);
		}
		if (depth >= REEVALUATION_ESTIMATION_DEPTH) {
			bestScore = Math.max(bestScore, evaluateAdvantage(this.b, this.b.localteam));
		}
		return bestScore;
	}

	/**
	 * Helper method (for getBestTurn) to perform a traversal of the decision tree,
	 * recursively exploring the subtree.
	 * 
	 * @param action     The action to perform
	 * @param turn       A list that will be modified into a chain of player actions
	 *                   leading to the best state, can be null if not necessary
	 * @param depth      The current traversal depth
	 * @param sampleRate The fraction of total possible actions that we sampled
	 * @return The score of the best board state traversed
	 */
	private double traverseAction(PlayerAction action, List<String> turn, int depth, double sampleRate) {
		double score = 0;
		if (action instanceof EndTurnAction || this.b.winner != 0 || this.b.currentPlayerTurn != this.b.localteam
				|| depth == REEVALUATION_MAX_DEPTH) {
			// no more actions can be taken
			score = evaluateAdvantage(this.b, this.b.localteam);
		} else {
			double nextSampleRate = sampleRate;
			if (depth + 1 == REEVALUATION_MIN_DEPTH) {
				nextSampleRate = REEVALUATION_SAMPLE_RATE;
			} else if (depth + 1 > REEVALUATION_MIN_DEPTH) {
				nextSampleRate = sampleRate * REEVALUATION_SAMPLE_RATE_MULTIPLIER;
			}
			score = this.getBestTurn(turn, depth + 1, nextSampleRate);
		}
		return score;
	}

	/**
	 * Given the current board state, attempts to find every possible action that
	 * the AI can make.
	 * 
	 * @return a list of possible actions taken by the AI
	 */
	private List<PlayerAction> getPossibleActions() {
		Player p = this.b.getPlayer(this.b.localteam);
		List<PlayerAction> poss = new LinkedList<PlayerAction>();
		List<BoardObject> minions = this.b.getBoardObjects(this.b.localteam, false, true, false);
		for (BoardObject b : minions) {
			// minion attack
			Minion m = (Minion) b;
			if (m.canAttack()) {
				for (Minion target : m.getAttackableTargets()) {
					poss.add(new OrderAttackAction(m, target));
				}
			}
			// unleashing cards & selecting targets
			if (p.canUnleashCard(m)) {
				if (!m.getUnleashTargets().isEmpty()) {
					List<List<Target>> targetSearchSpace = this.getPossibleListTargets(m.getUnleashTargets());
					for (List<Target> targets : targetSearchSpace) {
						poss.add(new UnleashMinionAction(p, m, Target.listToString(targets)));
					}
				} else { // no targets to set
					poss.add(new UnleashMinionAction(p, m, "0"));
				}
			}
		}
		// playing cards & selecting targets
		List<Card> hand = this.b.getPlayer(this.b.localteam).hand.cards;
		for (Card c : hand) {
			// TODO make it consider board positioning
			if (p.canPlayCard(c)) {
				if (!c.getBattlecryTargets().isEmpty()) {
					List<List<Target>> targetSearchSpace = this.getPossibleListTargets(c.getBattlecryTargets());
					for (List<Target> targets : targetSearchSpace) {
						poss.add(new PlayCardAction(p, c, 0, Target.listToString(targets)));
					}
				} else { // no targets to set
					poss.add(new PlayCardAction(p, c, 0, "0"));
				}
			}
		}
		// ending turn
		poss.add(new EndTurnAction(this.b.localteam));
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
	 * is the value that gets maximized by the ai.
	 * 
	 * @param team the team to evaluate for
	 * @return the advantage quantized as mana
	 */
	public static double evaluateAdvantage(Board b, int team) {
		return evaluateVictory(b, team) + evaluateSurvivability(b, team) + evaluateBoard(b, team)
				+ evaluateHand(b, team) - evaluateSurvivability(b, team * -1) - evaluateBoard(b, team * -1)
				- evaluateHand(b, team * -1);
	}

	/**
	 * Indicator that a player has achieved victory.
	 * 
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
}
