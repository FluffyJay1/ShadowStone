package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.unleashpower.*;

public class EventUnleash extends Event {
	public static final int ID = 16;
	public Card source;
	public Minion m;
	private int prevUnleashes;

	public EventUnleash(Card source, Minion m) {
		super(ID, false);
		this.source = source;
		this.m = m;
		this.priority = 1;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		eventlist.addAll(this.m.unleash());
		if (this.source instanceof UnleashPower) { // quality
			this.prevUnleashes = ((UnleashPower) this.source).unleashesThisTurn;
			((UnleashPower) this.source).unleashesThisTurn++;
		}
	}

	@Override
	public void undo() {
		if (this.source instanceof UnleashPower) { // quality
			((UnleashPower) this.source).unleashesThisTurn = this.prevUnleashes;
		}
	}

	@Override
	public String toString() {
		return this.id + " " + this.source.toReference() + m.toReference() + Target.listToString(m.getUnleashTargets())
				+ "\n";
	}

	public static EventUnleash fromString(Board b, StringTokenizer st) {
		Card source = Card.fromReference(b, st);
		Minion m = (Minion) Card.fromReference(b, st);
		Target.setListFromString(m.getUnleashTargets(), b, st);
		return new EventUnleash(source, m);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
