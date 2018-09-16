package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.Card;

public class EventGameEnd extends Event {

	public static final int ID = 28;
	public int victory;

	public EventGameEnd(int victory) {
		super(ID);
		this.priority = 10000;
		this.victory = victory;
		// TODO Auto-generated constructor stub
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		System.exit(0); // YES
	}

	public String toString() {
		return this.id + " " + victory + " \n";
	}

	public static EventGameEnd fromString(Board b, StringTokenizer st) {
		Card c = Card.fromReference(b, st);
		int vict = Integer.parseInt(st.nextToken());
		return new EventGameEnd(vict);
	}
}
