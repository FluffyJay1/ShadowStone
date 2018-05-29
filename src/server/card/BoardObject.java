package server.card;

import java.util.LinkedList;

import server.Board;
import server.card.effect.Effect;
import server.event.Event;

public class BoardObject extends Card {
	public int boardpos;

	public BoardObject(Board b, CardStatus status, int cost, String name, String text, String imagepath, int team,
			int id) {
		super(b, status, cost, name, text, imagepath, team, id);
		this.boardpos = 0;
	}

	public LinkedList<Event> lastWords() {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.lastWords());
		}
		return list;
	}

	public LinkedList<Event> onTurnStart() {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.onTurnStart());
		}
		return list;
	}

	public LinkedList<Event> onTurnEnd() {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.onTurnEnd());
		}
		return list;
	}

	public String posToString() {
		switch (this.status) {
		case HAND:
			return "hand " + this.handpos;
		case BOARD:
			return "board " + this.boardpos;
		case DECK:
			return "deck";
		default:
			return "";
		}
	}

	public String toString() {
		return "BoardObject " + name + " " + this.posToString() + " alive " + alive;
	}
}
