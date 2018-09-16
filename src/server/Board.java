package server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import client.Game;
import server.card.Amulet;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.Leader;
import server.card.Minion;
import server.card.Target;
import server.card.effect.EffectStats;
import server.event.*;

public class Board {
	public boolean isServer = true; // true means it is the center of game logic
	public boolean isClient; // true means has a visualboard
	// links cards created between board and visualboard
	public LinkedList<Card> cardsCreated = new LinkedList<Card>();

	public Player player1, player2;
	// localteam is the team of the player, i.e. at the bottom of the screen
	public int currentplayerturn = 1, localteam = 1;

	protected ArrayList<BoardObject> player1side;
	protected ArrayList<BoardObject> player2side;
	protected ArrayList<Card> player1graveyard;
	protected ArrayList<Card> player2graveyard;
	public ArrayList<Card> banished;
	public LinkedList<Event> eventlist;

	String output = "";

	public Board() {
		player1 = new Player(this, 1);
		player2 = new Player(this, -1);
		player1side = new ArrayList<BoardObject>();
		player2side = new ArrayList<BoardObject>();
		eventlist = new LinkedList<Event>();
		player1graveyard = new ArrayList<Card>();
		player2graveyard = new ArrayList<Card>();
		banished = new ArrayList<Card>();
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
		// ret.addAll(this.player1graveyard);
		// ret.addAll(this.player2graveyard);
		if (this.player1.unleashPower != null) {
			ret.add(this.player1.unleashPower);
		}
		if (this.player2.unleashPower != null) {
			ret.add(this.player2.unleashPower);
		}
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
		case GRAVEYARD:
			return this.getGraveyard(team);
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
			if (!ret.isEmpty() && ret.get(0) instanceof Leader) {
				ret.remove(0); // gotem
			}
		}
		if (nominion) {
			for (int i = 0; i < ret.size(); i++) {
				if (ret.get(i) instanceof Minion) {
					ret.remove(i); // don't worry about this
					i--;
				}
			}
		}
		if (noamulet) {
			for (int i = 0; i < ret.size(); i++) {
				if (ret.get(i) instanceof Amulet) {
					ret.remove(i);
					i--;
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

	public ArrayList<Card> getGraveyard(int team) {
		if (team == 1) {
			return this.player1graveyard;
		} else {
			return this.player2graveyard;
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

	// Only used by server, i.e. isServer == true
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
				if (e.resolvefirst) {
					LinkedList<Event> lul = new LinkedList<Event>();
					e.resolve(lul, loopprotection);
					eventlist.addAll(0, lul);
				} else {
					e.resolve(eventlist, loopprotection);
				}
				for (Card c : this.getCards()) {
					eventlist.addAll(c.onEvent(e));
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
		if (!s.isEmpty()) {
			String[] lines = s.split("\n");
			for (String line : lines) {
				StringTokenizer st = new StringTokenizer(line);
				Event e = Event.createFromString(this, st);
				if (e != null && e.conditions()) {
					LinkedList<Event> lmao = new LinkedList<Event>();
					e.resolve(lmao, false);
				}
			}
		}

	}

	public void playerPlayCard(Player p, Card c, int pos, String btstring) {
		if (!p.canPlayCard(c)) { // just to be safe
			return;
		}
		if (btstring != null) {
			StringTokenizer st = new StringTokenizer(btstring);
			c.battlecryTargetsFromString(this, st);
		}
		this.eventlist.add(new EventPlayCard(p, c, pos));
		this.resolveAll();
	}

	public void playerUnleashMinion(Player p, Minion m, String utstring) {
		if (!p.canUnleashCard(m)) {
			return;
		}
		if (utstring != null) {
			StringTokenizer st = new StringTokenizer(utstring);
			m.unleashTargetsFromString(this, st);
		}
		this.eventlist
				.add(new EventManaChange(p, -p.unleashPower.finalStatEffects.getStat(EffectStats.COST), false, true));
		this.eventlist.addAll(p.unleashPower.unleash(m));
		this.resolveAll();
	}

	// encapsulation at its finest
	public void playerOrderAttack(Minion attacker, Minion victim) {
		if (attacker.getAttackableTargets().contains(victim)) {
			this.eventlist.add(new EventMinionAttack(attacker, victim));
			this.resolveAll();
		}
	}

	public void playerEndTurn(int team) {
		if (team == this.currentplayerturn) {
			this.endCurrentPlayerTurn();
		}
	}

	public void AIThink() {
		for (int i = 1; i < this.getBoardObjects(this.localteam * -1).size(); i++) {
			BoardObject bo = this.getBoardObject(this.localteam * -1, i);
			if (bo instanceof Minion) {

				ArrayList<Minion> targets = ((Minion) bo).getAttackableTargets();
				if (targets.get(0) instanceof Leader) {
					// smorc
					this.playerOrderAttack((Minion) bo, targets.get(0));
				} else {
					// ward is cheat
					this.playerOrderAttack((Minion) bo, Game.selectRandom(targets));
				}
				if (!bo.alive) {
					i--;
				}
			}
		}
		int tempmana = Math.min(this.getPlayer(this.localteam * -1).maxmana,
				this.getPlayer(this.localteam * -1).maxmaxmana); // mm
		for (int i = 0; i < 10; i++) {
			if (!this.getPlayer(this.localteam * -1).hand.cards.isEmpty()) {
				Card c = Game.selectRandom(this.getPlayer(this.localteam * -1).hand.cards);
				if (c.conditions() && tempmana >= c.finalStatEffects.getStat(EffectStats.COST)) {
					for (Target t : c.getBattlecryTargets()) {
						t.fillRandomTargets();
					}
					int randomind = (int) (Math.random() * (this.getBoardObjects(this.localteam * -1).size() - 1)) + 1;
					this.playerPlayCard(this.getPlayer(this.localteam * -1), c, randomind, null);
					tempmana -= c.finalStatEffects.getStat(EffectStats.COST);
				}
			}
		}
		this.playerEndTurn(this.localteam * -1);
	}

	public void endCurrentPlayerTurn() {
		System.out.println("ENDING PLAYER " + this.currentplayerturn + " TURN");
		this.eventlist.add(new EventTurnEnd(this.getPlayer(this.currentplayerturn)));
		this.resolveAll();
		this.eventlist.add(new EventTurnStart(this.getPlayer(this.currentplayerturn * -1)));
		this.resolveAll();
	}
}
