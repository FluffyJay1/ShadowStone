package server;

import java.util.*;

import client.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.playeraction.*;

public class Board {
	public boolean ai = false;
	public boolean isServer = true; // true means it is the center of game logic
	public boolean isClient; // true means has a visualboard
	// links cards created between board and visualboard
	public LinkedList<Card> cardsCreated = new LinkedList<Card>();

	public Player player1, player2;
	// localteam is the team of the player, i.e. at the bottom of the screen
	public int currentplayerturn = 1, localteam = 1, winner = 0;

	protected List<BoardObject> player1side;
	protected List<BoardObject> player2side;
	protected List<Card> player1graveyard;
	protected List<Card> player2graveyard;
	public List<Card> banished;
	public List<Event> eventlist;

	String output = "";
	List<String> playerActions;

	public Board() {
		player1 = new Player(this, 1);
		player2 = new Player(this, -1);
		player1side = new ArrayList<BoardObject>();
		player2side = new ArrayList<BoardObject>();
		eventlist = new LinkedList<Event>();
		player1graveyard = new ArrayList<Card>();
		player2graveyard = new ArrayList<Card>();
		banished = new ArrayList<Card>();
		playerActions = new LinkedList<String>();
	}

	public Board(int localteam) {
		this();
		this.localteam = localteam;
	}

	public Player getPlayer(int team) {
		return team == 1 ? player1 : player2;
	}

	public List<Card> getTargetableCards() {
		List<Card> ret = new ArrayList<Card>();
		ret.addAll(this.player1side);
		ret.addAll(this.player2side);
		ret.addAll(this.player1.hand.cards);
		ret.addAll(this.player2.hand.cards);
		return ret;
	}

	public List<Card> getTargetableCards(Target t) {
		List<Card> list = new LinkedList<Card>();
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

	public List<Card> getCards() {
		List<Card> ret = new ArrayList<Card>();
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
	public List<Card> getCollection(int team, CardStatus status) {
		switch (status) {
		case HAND:
			return this.getPlayer(team).hand.cards;
		case BOARD:
			List<Card> cards = new ArrayList<Card>();
			cards.addAll(this.getBoardObjects(team)); // gottem
			return cards;
		case DECK:
			return this.getPlayer(team).deck.cards;
		case GRAVEYARD:
			return this.getGraveyard(team);
		case UNLEASHPOWER:
			List<Card> cards2 = new ArrayList<Card>();
			cards2.add(this.getPlayer(team).unleashPower);
			return cards2;
		case LEADER:
			List<Card> cards3 = new ArrayList<Card>();
			cards3.add(this.getPlayer(team).leader);
			return cards3;
		default:
			return null;
		}
	}

	public List<BoardObject> getBoardObjects() {
		ArrayList<BoardObject> ret = new ArrayList<BoardObject>();
		ret.addAll(this.player1side);
		ret.addAll(this.player2side);
		return ret;
	}

	public List<BoardObject> getBoardObjects(int team) {
		// i saw what i was trying to do here originally and it's bad
		ArrayList<BoardObject> ret = new ArrayList<BoardObject>();
		if (team > 0) {
			return this.player1side;
		} else {
			return this.player2side;
		}
	}

	public List<BoardObject> getBoardObjects(int team, boolean leader, boolean minion, boolean amulet) {
		ArrayList<BoardObject> ret = new ArrayList<BoardObject>();
		if (team >= 0) {
			ret.addAll(this.player1side);
		}
		if (team <= 0) {
			ret.addAll(this.player2side);
		}
		if (leader && this.getPlayer(team).leader != null) {
			ret.add(this.getPlayer(team).leader);
		}
		if (!minion) {
			for (int i = 0; i < ret.size(); i++) {
				if (ret.get(i) instanceof Minion) {
					ret.remove(i); // don't worry about this
					i--;
				}
			}
		}
		if (!amulet) {
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
		List<BoardObject> relevantSide = team > 0 ? player1side : player2side;
		if (position >= relevantSide.size() || position < 0) {
			return null;
		}
		return relevantSide.get(position);
	}

	public void addBoardObject(BoardObject bo, int team, int position) {
		List<BoardObject> relevantSide = team > 0 ? player1side : player2side;
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
		List<BoardObject> relevantSide = team > 0 ? player1side : player2side;
		if (position >= relevantSide.size()) {
			return;
		}
		bo = relevantSide.remove(position);
		for (int i = position; i < relevantSide.size(); i++) {
			relevantSide.get(i).cardpos--;
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

	public List<Card> getGraveyard(int team) {
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

	public List<Event> resolveAll() {
		return this.resolveAll(this.eventlist, false);
	}

	// Only used by server, i.e. isServer == true
	public List<Event> resolveAll(List<Event> eventlist, boolean loopprotection) {
		LinkedList<Event> l = new LinkedList<Event>();
		while (!eventlist.isEmpty()) {
			Event e = eventlist.remove(0);
			l.add(e);
			if (e.conditions()) {
				String eventstring = e.toString();
				if (!eventstring.isEmpty() && e.send) {
					// System.out.println(eventstring);
					this.output += eventstring;
					System.out.print(e.getClass().getName() + ": " + eventstring);
					// System.out.println(this.stateToString());
				}
				if (e.priority > 0) {
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
		return l;
	}

	public String retrieveEventString() {
		String temp = this.output;
		this.output = "";
		return temp;
	}

	public synchronized String retrievePlayerAction() {
		if (!this.playerActions.isEmpty()) {
			return this.playerActions.remove(0);
		}
		return null;
	}

	public synchronized void parseEventString(String s) {
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

	public synchronized boolean executePlayerAction(StringTokenizer st) {
		PlayerAction pa = PlayerAction.createFromString(this, st);
		return pa.perform(this);
	}

	private void sendAction(PlayerAction action) {
		if (this.isServer) {
			action.perform(this);
		} else {
			this.playerActions.add(action.toString());
		}
	}

	public void playerPlayCard(Player p, Card c, int pos) {
		this.sendAction(new PlayCardAction(p, c, pos));
	}

	public void playerUnleashMinion(Player p, Minion m) {
		this.sendAction(new UnleashMinionAction(p, m));
	}

	// encapsulation at its finest
	public void playerOrderAttack(Minion attacker, Minion victim) {
		this.sendAction(new OrderAttackAction(attacker, victim));
	}

	public void playerEndTurn(int team) {
		this.sendAction(new EndTurnAction(team));
	}

	public void AIThink() {
		for (int i = 0; i < this.getBoardObjects(this.localteam * -1).size(); i++) {
			BoardObject bo = this.getBoardObject(this.localteam * -1, i);
			if (bo instanceof Minion) {

				List<Minion> targets = ((Minion) bo).getAttackableTargets();
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
					int randomind = (int) (Math.random() * (this.getBoardObjects(this.localteam * -1).size()));
					this.playerPlayCard(this.getPlayer(this.localteam * -1), c, randomind);
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
		if (this.ai && this.currentplayerturn == this.localteam * -1) {
			this.AIThink();
		}
	}
}
