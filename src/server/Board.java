package server;

import java.util.ArrayList;
import java.util.LinkedList;

import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.Leader;
import server.card.Minion;
import server.card.Target;
import server.event.*;

public class Board {
	public Player player1, player2;
	public int currentplayerturn = 1;
	protected ArrayList<BoardObject> player1side;
	protected ArrayList<BoardObject> player2side;
	public LinkedList<Event> eventlist;

	public Board() {
		player1 = new Player(this, 1);
		player2 = new Player(this, -1);
		player1side = new ArrayList<BoardObject>();
		player2side = new ArrayList<BoardObject>();
		eventlist = new LinkedList<Event>();
		this.addBoardObjectToSide(new Leader(this, "Rowen", "The Memer"), 1);
		this.addBoardObjectToSide(new Leader(this, "Nower", "Remem eht"), -1);
		this.eventlist.add(new EventDraw(player1, 3));
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

	public ArrayList<BoardObject> getBoardObjects() {
		ArrayList<BoardObject> ret = new ArrayList<BoardObject>();
		ret.addAll(this.player1side);
		ret.addAll(this.player2side);
		return ret;
	}

	public ArrayList<BoardObject> getBoardObjects(int team) {
		ArrayList<BoardObject> ret = new ArrayList<BoardObject>();
		ret.addAll(team > 0 ? this.player1side : this.player2side);
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
				if (!eventstring.isEmpty()) {
					System.out.println(eventstring);
					// System.out.println(this.stateToString());
				}
				e.resolve(eventlist, loopprotection);
			}
		}
	}

	public void endPlayerTurn(int team) {
		if (team == this.currentplayerturn) { // a level security
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
		this.endCurrentPlayerTurn();
	}

	public void endCurrentPlayerTurn() {
		System.out.println("ENDING PLAYER " + this.currentplayerturn + " TURN");
		this.eventlist.add(new EventTurnEnd(this.getPlayer(this.currentplayerturn)));
		this.resolveAll();
		this.eventlist.add(new EventTurnStart(this.getPlayer(this.currentplayerturn * -1)));
		this.resolveAll();
	}
}
