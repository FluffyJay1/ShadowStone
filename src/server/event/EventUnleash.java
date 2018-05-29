package server.event;

import java.util.LinkedList;

import server.Player;
import server.card.Minion;

public class EventUnleash extends Event {
	Player p;
	Minion m;
	boolean success = false;

	public EventUnleash(Player p, Minion m) {
		this.p = p;
		this.m = m;
	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		if (!this.conditions()) {
			return this.toString();
		}
		// add the unleash effects of individual classes
		eventlist.add(new EventManaChange(this.p, -2, false, true));
		eventlist.addAll(this.m.unleash());
		this.p.unleashedThisTurn = true;
		this.success = true;
		return this.toString();
	}

	public String toString() {
		return "unleash " + this.conditions() + m.toString() + " " + m.unleashTargetsToString();
	}

	public boolean conditions() {
		return this.p.canUnleashCard(this.m) || this.success;
	}
}
