package server.event;

import java.util.*;

import server.*;
import server.card.*;

public class EventEnterPlay extends Event {
	public static final int ID = 23;
	public Card c;

	// this is just used as a hook for effects
	public EventEnterPlay(Card c) {
		super(ID, false);
		this.c = c;
		this.priority = 1;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		eventlist.addAll(((BoardObject) this.c).onEnterPlay());
	}

	@Override
	public void undo() {
		// nothing much we can do here either
	}

	@Override
	public String toString() {
		return this.id + " " + this.c.toReference() + "\n";
	}

	public static EventEnterPlay fromString(Board b, StringTokenizer st) {
		Card c = Card.fromReference(b, st);
		return new EventEnterPlay(c);
	}

	@Override
	public boolean conditions() {
		return this.c instanceof BoardObject;
	}
}
