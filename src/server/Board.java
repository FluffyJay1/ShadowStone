package server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import client.Game;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.Leader;
import server.card.Minion;
import server.card.Target;
import server.card.effect.EffectStats;
import server.event.*;

public class Board {
	public boolean isClient; // true means has a visualboard
	// links cards created between board and visualboard
	public LinkedList<Card> cardsCreated = new LinkedList<Card>();

	public Player player1, player2;
	public int currentplayerturn = 1;

	protected ArrayList<BoardObject> player1side;
	protected ArrayList<BoardObject> player2side;
	public LinkedList<Event> eventlist;
	protected LinkedList<String> inputeventliststrings = new LinkedList<String>();
	String output = "";

	public Board() {
		player1 = new Player(this, 1);
		player2 = new Player(this, -1);
		player1side = new ArrayList<BoardObject>();
		player2side = new ArrayList<BoardObject>();
		eventlist = new LinkedList<Event>();

	}

	public Player getPlayer(int team) {
		return team == 1 ? player1 : player2;
	}

	public ArrayList<Card> getTargetableCards() {
		ArrayList<Card> ret = new ArrayList<Card>();
		ret.addAll(this.player1side);
		ret.addAll(this.player2side);
		ret.addAll(this.player1.hand.cards);
		ret.addAll(this.player2.hand.cards);
		return ret;
	}

	public LinkedList<Card> getTargetableCards(Target t) {
		LinkedList<Card> list = new LinkedList<Card>();
		if (t == null) {
			return list;
		}
		for (Card c : this.getTargetableCards()) {
			if (t.canTarget(c)) {
				list.add(c);
			}
		}
		return list;
	}

	public ArrayList<Card> getCards() {
		ArrayList<Card> ret = new ArrayList<Card>();
		ret.addAll(this.getBoardObjects());
		ret.addAll(this.player1.hand.cards);
		ret.addAll(this.player1.deck.cards);
		ret.addAll(this.player2.hand.cards);
		ret.addAll(this.player2.deck.cards);
		return ret;
	}

	// i don't even know what this is
	public ArrayList<Card> getCollection(int team, CardStatus status) {
		switch (status) {
		case HAND:
			return this.getPlayer(team).hand.cards;
		case BOARD:
			ArrayList<Card> cards = new ArrayList<Card>();
			cards.addAll(this.getBoardObjects(team)); // gottem
			return cards;
		case DECK:
			return this.getPlayer(team).deck.cards;
		default:
			return null;
		}
	}

	public ArrayList<BoardObject> getBoardObjects() {
		ArrayList<BoardObject> ret = new ArrayList<BoardObject>();
		ret.addAll(this.player1side);
		ret.addAll(this.player2side);
		return ret;
	}

	public ArrayList<BoardObject> getBoardObjects(int team) {
		// i saw what i was trying to do here originally and it's bad
		ArrayList<BoardObject> ret = new ArrayList<BoardObject>();
		if (team > 0) {
			return this.player1side;
		} else {
			return this.player2side;
		}
	}

	public ArrayList<BoardObject> getBoardObjects(int team, boolean noleader, boolean nominion, boolean noamulet) {
		ArrayList<BoardObject> ret = new ArrayList<BoardObject>();
		if (team >= 0) {
			ret.addAll(this.player1side);
		}
		if (team <= 0) {
			ret.addAll(this.player2side);
		}
		if (noleader) {
			ret.remove(0); // gotem
		}
		if (nominion) {
			for (BoardObject b : ret) {
				if (b instanceof Minion) {
					ret.remove(b); // don't worry about this
				}
			}
		}
		if (noamulet) { // TODO IMPLEMENT AMULET
			for (BoardObject b : ret) {
				if (!(b instanceof Minion)) {
					ret.remove(b);
				}
			}
		}
		return ret;
	}

	public BoardObject getBoardObject(int team, int position) {
		ArrayList<BoardObject> relevantSide = team > 0 ? player1side : player2side;
		if (position >= relevantSide.size()) {
			return null;
		}
		return relevantSide.get(position);
	}

	public void addBoardObject(BoardObject bo, int team, int position) {
		ArrayList<BoardObject> relevantSide = team > 0 ? player1side : player2side;
		if (position > relevantSide.size()) {
			position = relevantSide.size();
		}
		bo.cardpos = position;
		bo.status = CardStatus.BOARD;
		bo.team = team;
		relevantSide.add(position, bo);
		for (int i = position + 1; i < relevantSide.size(); i++) {
			relevantSide.get(i).cardpos++;
		}
	}

	public void addBoardObjectToSide(BoardObject bo, int team) {
		bo.team = team;
		if (team > 0) {
			addBoardObject(bo, 1, player1side.size());
		}
		if (team < 0) {
			addBoardObject(bo, -1, player2side.size());
		}
	}

	public void removeBoardObject(BoardObject bo) {
		if (this.player1side.contains(bo)) {
			this.player1side.remove(bo);
		}
		if (this.player2side.contains(bo)) {
			this.player2side.remove(bo);
		}
		this.updatePositions();
	}

	public void removeBoardObject(int team, int position) {
		BoardObject bo;
		ArrayList<BoardObject> relevantSide = team > 0 ? player1side : player2side;
		if (position >= relevantSide.size()) {
			return;
		}
		if (position == 0) { // leader dies

		} else {
			bo = relevantSide.remove(position);
			for (int i = position; i < relevantSide.size(); i++) {
				relevantSide.get(i).cardpos--;
			}
		}
	}

	public void updatePositions() {
		for (int i = 0; i < player1side.size(); i++) {
			player1side.get(i).cardpos = i;
		}
		for (int i = 0; i < player2side.size(); i++) {
			player2side.get(i).cardpos = i;
		}
	}

	public String stateToString() {
		String ret = "State----------------------------+\nPlayer 1:\n";
		for (BoardObject b : player1side) {
			ret += b.toString() + "\n";
		}
		ret += "\nPlayer 2:\n";
		for (BoardObject b : player2side) {
			ret += b.toString() + "\n";
		}
		ret += "---------------------------------+\n";
		return ret;
	}

	public void resolveAll() {
		this.resolveAll(this.eventlist, false);
	}

	public void resolveAll(LinkedList<Event> eventlist, boolean loopprotection) {
		while (!eventlist.isEmpty()) {
			Event e = eventlist.removeFirst();
			if (e.conditions()) {
				String eventstring = e.toString();
				if (!eventstring.isEmpty() && e.send) {
					// System.out.println(eventstring);
					this.output += eventstring;
					// System.out.println(this.stateToString());
				}
				e.resolve(eventlist, loopprotection);
				for (Card c : this.getCards()) {
					c.onEvent(e);
				}
			}
		}
	}

	public String retrieveEventString() {
		String temp = this.output;
		this.output = "";
		return temp;
	}

	public void parseEventString(String s) {
		LinkedList<Event> list = new LinkedList<Event>();
		StringTokenizer st = new StringTokenizer(s, "\n");
		System.out.println("EVENTSTRING:");
		System.out.println(s);

		while (st.hasMoreTokens()) {
			String event = st.nextToken();
			this.inputeventliststrings.add(event);
		}
	}

	public void playerPlayCard(Player p, Card c, int pos, String btstring) {
		StringTokenizer st = new StringTokenizer(btstring);
		c.battlecryTargetsFromString(this, st);
		this.eventlist.add(new EventPlayCard(p, c, pos));
		this.resolveAll();
	}

	public void playerUnleashMinion(Player p, Minion m, String utstring) {
		StringTokenizer st = new StringTokenizer(utstring);
		m.unleashTargetsFromString(this, st);
		this.eventlist.add(new EventUnleash(p, m));
		this.resolveAll();
	}

	// encapsulation at its finest
	public void playerOrderAttack(Minion attacker, Minion victim) {
		this.eventlist.add(new EventMinionAttack(attacker, victim));
		this.resolveAll();
	}

	public void playerEndTurn(int team) {
		if (team == this.currentplayerturn) {
			this.endCurrentPlayerTurn();
		}
	}

	public void AIThink() {
		for (BoardObject bo : this.getBoardObjects(-1)) {
			if (bo instanceof Minion) {
				// smorc
				this.eventlist.add(new EventMinionAttack((Minion) bo, (Minion) this.getBoardObject(1, 0)));
				this.resolveAll();
			}
		}
		int tempmana = Math.min(this.player2.maxmana, this.player2.maxmaxmana); // mm
		for (int i = 0; i < 10; i++) {
			if (!this.player2.hand.cards.isEmpty()) {
				Card c = Game.selectRandom(this.player2.hand.cards);
				if (c.conditions() && tempmana >= c.finalStatEffects.getStat(EffectStats.COST)) {
					for (Target t : c.getBattlecryTargets()) {
						t.fillRandomTargets();
					}
					int randomind = (int) (Math.random() * (this.player2side.size() - 1)) + 1;
					this.eventlist.add(new EventPlayCard(this.player2, c, randomind));
					tempmana -= c.finalStatEffects.getStat(EffectStats.COST);
					this.resolveAll();
				}
			}
		}
		this.playerEndTurn(-1);
	}

	public void endCurrentPlayerTurn() {
		System.out.println("ENDING PLAYER " + this.currentplayerturn + " TURN");
		this.eventlist.add(new EventTurnEnd(this.getPlayer(this.currentplayerturn)));
		this.resolveAll();
		this.eventlist.add(new EventTurnStart(this.getPlayer(this.currentplayerturn * -1)));
		this.resolveAll();
	}
}
