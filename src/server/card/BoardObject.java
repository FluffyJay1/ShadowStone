package server.card;

import java.util.LinkedList;

import server.Board;
import server.event.Event;

public class BoardObject extends Card {
	public boolean alive;
	public int position;
	public int team;

	public BoardObject(Board b, CardStatus status, int cost, String name, String text, String imagepath, int id) {
		super(b, status, cost, name, text, imagepath, id);
		this.alive = true;
		this.position = 0;
	}

	public LinkedList<Event> lastWords() {
		return new LinkedList<Event>();
	}

	public String toString() {
		return "BoardObject " + name + " cost " + cost + " position " + position + " alive " + alive;
	}
}
