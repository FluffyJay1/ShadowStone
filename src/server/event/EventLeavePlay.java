package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.BoardObject;
import server.card.Card;

public class EventLeavePlay extends Event {
	public static final int ID = 24;
	public Card c;

	// this is just used as a hook for effects
	public EventLeavePlay(Card c) {
		super(ID);
		this.c = c;
		this.priority = 1;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		eventlist.addAll(((BoardObject) this.c).onLeavePlay());
	}

	public String toString() {
		return this.id + " " + this.c.toReference() + "\n";
	}

	public static EventLeavePlay fromString(Board b, StringTokenizer st) {
		Card c = Card.fromReference(b, st);
		return new EventLeavePlay(c);
	}

	public boolean conditions() {
		return this.c instanceof BoardObject;
	}
}
