package server.card;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.effect.Effect;
import server.event.Event;

public class BoardObject extends Card {

	public BoardObject(Board b, CardStatus status, int cost, String name, String text, String imagepath, int team,
			int id) {
		super(b, status, cost, name, text, imagepath, team, id);
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

	public String toString() {
		return "BoardObject " + name + " " + this.cardPosToString() + " ";
	}

}
