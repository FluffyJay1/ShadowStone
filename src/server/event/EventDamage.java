package server.event;

import java.util.LinkedList;

import server.card.Minion;

public class EventDamage extends Event {
	// whenever damage is dealt
	int damage;
	Minion m;

	public EventDamage(Minion m, int damage) {
		this.m = m;
		this.damage = damage;
	}

	@Override
	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.m.stats.h -= damage;
		if (!loopprotection) {
			eventlist.addAll(m.onDamaged(damage));
		}
		if (m.stats.h <= 0) {
			eventlist.add(new EventDestroy(m));
		}
		return this.toString();
	}

	@Override
	public String toString() {
		return "dmg " + m.boardpos + " " + damage + "\n";
	}
}
