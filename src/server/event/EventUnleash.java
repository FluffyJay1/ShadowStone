package server.event;

import java.util.LinkedList;

import server.Player;
import server.card.Minion;

public class EventUnleash extends Event {
	Player p;
	Minion m;

	public EventUnleash(Player p, Minion m) {
		this.p = p;
		this.m = m;
	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		if (!this.conditions()) {
			return this.toString();
		}
		// add the unleash effects of individual classes
		eventlist.addAll(this.m.unleash());
		return this.toString();
	}

	public String toString() {
		return "unleash " + m.toString();
	}

	public boolean conditions() {
		return m.alive;
	}
}
