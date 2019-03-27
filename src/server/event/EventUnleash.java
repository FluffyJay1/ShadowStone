package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.unleashpower.*;

public class EventUnleash extends Event {
	public static final int ID = 16;
	public Card source;
	public Minion m;

	public EventUnleash(Card source, Minion m) {
		super(ID);
		this.source = source;
		this.m = m;
		this.priority = 1;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		eventlist.addAll(this.m.unleash());
		if (this.source instanceof UnleashPower) { // quality
			((UnleashPower) this.source).unleashesThisTurn++;
		}
	}

	@Override
	public String toString() {
		return this.id + " " + this.source.toReference() + m.toReference() + m.unleashTargetsToString() + "\n";
	}

	public static EventUnleash fromString(Board b, StringTokenizer st) {
		Card source = Card.fromReference(b, st);
		Minion m = (Minion) Card.fromReference(b, st);
		m.unleashTargetsFromString(b, st); // TODO no need?
		return new EventUnleash(source, m);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
