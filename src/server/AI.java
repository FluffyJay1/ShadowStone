package server;

import java.util.*;

import client.*;
import network.*;
import server.card.*;
import server.card.cardpack.basic.*;
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
	int difficulty;
	Board b;
	DataStream dslocal;
	boolean hasThought; // if ai has done its processing this turn

	public AI(DataStream dslocal, int team, int difficulty) {
		this.difficulty = difficulty;
		this.dslocal = dslocal;
		this.b = new Board(team);
	}

	@Override
	public void run() {
		while (this.b.winner == 0) {
			int prevturn = this.b.currentPlayerTurn;
			this.readDataStream();
			boolean turnChanged = prevturn != this.b.currentPlayerTurn;
			if (this.b.currentPlayerTurn == this.b.localteam && !this.hasThought) {
				this.AIThink();
				this.hasThought = true;
			} else if (turnChanged) {
				this.hasThought = false;
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
				break;
			default:
				break;
			}
		}
	}

	private void AIThink() {
		System.out.println("advantage: " + this.evaluateAdvantage(this.b.localteam));
		List<Event> undoStack = new LinkedList<Event>();
		List<String> actionStack = new LinkedList<String>();
		String stateBefore = this.b.stateToString();
		while (true) {
			List<PlayerAction> poss = this.getPossibleActions();
			PlayerAction action = Game.selectRandom(poss);
			actionStack.add(action.toString());
			List<Event> happenings = this.b.executePlayerAction(new StringTokenizer(action.toString()));
			undoStack.addAll(happenings);
			if (action instanceof EndTurnAction) {
				break;
			}
		}
		while (!undoStack.isEmpty()) {
			undoStack.get(undoStack.size() - 1).undo();
			undoStack.remove(undoStack.size() - 1);
		}
		String stateAfter = this.b.stateToString();
		if (!stateBefore.equals(stateAfter)) {
			System.out.println("SOMETHINGS WRONG");
		}
		for (String action : actionStack) {
			this.dslocal.sendPlayerAction(action);
		}
	}

	/**
	 * Given the current board state, attempts to find every possible action
	 * that the AI can make.
	 * 
	 * @return a list of possible actions taken by the AI
	 */
	private List<PlayerAction> getPossibleActions() {
		// minion attack
		Player p = this.b.getPlayer(this.b.localteam);
		List<PlayerAction> poss = new LinkedList<PlayerAction>();
		List<BoardObject> minions = this.b.getBoardObjects(this.b.localteam, false, true, false);
		for (BoardObject b : minions) {
			Minion m = (Minion) b;
			for (Minion target : m.getAttackableTargets()) {
				poss.add(new OrderAttackAction(m, target));
			}
		}
		// playing cards & selecting targets
		List<Card> hand = this.b.getPlayer(this.b.localteam).hand.cards;
		for (Card c : hand) {
			// sweet lord
			// TODO make it consider board positioning
			if (p.canPlayCard(c)) {
				if (c instanceof Fireball) {
					System.out.println("gamer moment");
				}
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
		// unleashing cards & selecting targets
		// ending turn
		poss.add(new EndTurnAction(this.b.localteam));
		return poss;
	}

	/**
	 * Given the current board state and a targeting requirement to fulfill,
	 * recursively finds every possible combination of targets
	 * 
	 * @param t
	 *            The target to fill
	 * @return A list of possible targets
	 */
	private List<Target> getPossibleTargets(Target t) {
		// TODO OPTIMIZE SO IT DOESN'T SEARCH THE SAME COMBINATION TWICE
		List<Target> poss = new LinkedList<Target>();
		if (this.b.isFullyTargeted(t)) {
			poss.add(t);
			return poss;
		}
		List<Card> searchspace = this.b.getTargetableCards(t);
		for (Card c : searchspace) {
			if (!t.getTargets().contains(c)) {
				Target copy = t.clone();
				copy.setTarget(c);
				poss.addAll(this.getPossibleTargets(copy));
			}
		}
		return poss;
	}

	/**
	 * Given the current board state and a list of targets, recursively finds
	 * every possible combination of target combos, e.g. if a card has 3
	 * battlecry effects each with their own targeting scheme, this will find
	 * every possible combination of targets of those 3 effects, like (0, 1, 2),
	 * (0, 1, 3), (0, 2, 1), etc.
	 * 
	 * @param list
	 *            The list of targets to fill
	 * @return A list of possible sets of targets. Each element in the list is a
	 *         set of targets to be used by the card.
	 */
	private List<List<Target>> getPossibleListTargets(List<Target> list) {
		// TODO OPTIMIZE SO IT DOESN'T SEARCH THE SAME COMBINATION TWICE
		List<List<Target>> poss = new LinkedList<List<Target>>();
		if (list.isEmpty()) {
			return poss;
		}
		List<Target> searchspace = this.getPossibleTargets(list.get(0));
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
	 * Attempts to estimate the advantage of a player in terms of mana value.
	 * This is the value that gets maximized by the ai.
	 * 
	 * @param team
	 *            the team to evaluate for
	 * @return the advantage quantized as mana
	 */
	public double evaluateAdvantage(int team) {
		// because i dont want implement hand tracking by the ai, im just gonna
		// let the ai see the opponents hand lmao
		return this.evaluateSurvivability(team) + this.evaluateBoard(team) + this.evaluateHand(team)
				- this.evaluateSurvivability(team * -1) - this.evaluateBoard(team * -1) - this.evaluateHand(team * -1);
	}

	/**
	 * Health is a resource, attempts to evaluate the mana worth of having high
	 * hp/opponent at low hp, factoring in enemy minions and friendly wards. The
	 * formula is mana = 6ln(ehp), making a 12hp heal worth about 5 mana when at
	 * 9 hp, equivalent to greater healing potion, and a 6hp nuke worth about 5
	 * mana when opponent is at 11 hp, equivalent to fireball
	 * 
	 * @param team
	 *            the team to evaluate for
	 * @return The approximate mana value of that leader's survivability
	 */
	public double evaluateSurvivability(int team) {
		if (this.b.getPlayer(team).leader.health <= 0) { // if dead
			return -99999999; // u dont want to be dead
		}
		int potentialDamage = 0, ehp = this.b.getPlayer(team).leader.health, attackers = 0, defenders = 0;
		for (BoardObject bo : this.b.getBoardObjects(team * -1, false, true, false)) {
			// TODO add if can attack check
			Minion m = (Minion) bo;
			// TODO factor in damage limiting effects like durandal
			potentialDamage += m.finalStatEffects.getStat(EffectStats.ATTACK)
					* m.finalStatEffects.getStat(EffectStats.ATTACKS_PER_TURN);
			attackers += m.finalStatEffects.getStat(EffectStats.ATTACKS_PER_TURN);
		}
		for (BoardObject bo : this.b.getBoardObjects(team * -1, false, true, false)) {
			Minion m = (Minion) bo;
			if (m.finalStatEffects.getStat(EffectStats.WARD) > 0) {
				ehp += m.health;
				defenders++;
			}
		}

		ehp -= potentialDamage;

		// if there are more defenders than attacks, then minions shouldn't be
		// able to touch face
		if (defenders >= attackers) {
			ehp = Math.max(ehp, this.b.getPlayer(team).leader.health);
		}

		if (ehp < 0) {
			return -9999999; // yeah don't be dead
		}
		return 6 * Math.log(ehp);
	}

	/**
	 * Attempts to put a mana value on a boardstate, basically just the sum of
	 * the values of each board object
	 * 
	 * @param team
	 *            the team to evaluate for
	 * @return the approximate mana value
	 */
	public double evaluateBoard(int team) {
		double total = 0;
		for (BoardObject bo : this.b.getBoardObjects(team)) {
			total += bo.getValue();
		}
		return total;
	}

	/**
	 * More cards in hand is better, but you also want playable stuff. The power
	 * of a card is value/(cost + 1) + 1, which usually comes to about 2 mana
	 * per card. A more powerful hand would have higher value cards with lower
	 * cost.
	 * 
	 * @param team
	 *            the team to evaluate for. can cheat by looking at opponents
	 *            hand lol
	 * @return the mana value of having these cards in hand
	 */
	public double evaluateHand(int team) {
		Hand hand = this.b.getPlayer(team).hand;
		double totalPower = 0;
		for (Card c : hand.cards) {
			totalPower += c.getValue() / (c.finalStatEffects.getStat(EffectStats.COST) + 1) + 1;
		}
		return totalPower;
	}
}
