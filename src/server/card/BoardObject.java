package server.card;

import java.util.LinkedList;

import server.Board;
import server.event.Event;

public class BoardObject extends Card {
	public boolean alive;
	public int boardpos;

	public BoardObject(Board b, CardStatus status, int cost, String name, String text, String imagepath, int team,
			int id) {
		super(b, status, cost, name, text, imagepath, team, id);
		this.alive = true;
		this.boardpos = 0;
	}

	public LinkedList<Event> lastWords() {
		return new LinkedList<Event>();
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
