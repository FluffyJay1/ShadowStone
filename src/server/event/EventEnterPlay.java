package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.BoardObject;
import server.card.Card;

public class EventEnterPlay extends Event {
	public static final int ID = 23;
	public Card c;

	// this is just used as a hook for effects
	public EventEnterPlay(Card c) {
		super(ID);
		this.c = c;
		this.resolvefirst = true;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		eventlist.addAll(((BoardObject) this.c).onEnterPlay());
	}

	public String toString() {
		return this.id + " " + this.c.toReference() + "\n";
	}

	public static EventEnterPlay fromString(Board b, StringTokenizer st) {
		Card c = Card.fromReference(b, st);
		return new EventEnterPlay(c);
	}

	public boolean conditions() {
		return this.c instanceof BoardObject;
	}
}
