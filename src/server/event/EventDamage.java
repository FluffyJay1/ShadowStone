package server.event;

import java.util.LinkedList;

import server.card.Minion;
import server.card.effect.EffectStats;

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
		if (!this.conditions()) {
			return this.toString();
		}
		this.m.health -= damage;
		if (!loopprotection) {
			eventlist.addAll(m.onDamaged(damage));
		}
		if (m.health <= 0) {
			eventlist.add(new EventDestroy(m));
		}
		return this.toString();
	}

	@Override
	public String toString() {
		return "dmg " + m.boardpos + " " + damage + "\n";
	}

	@Override
	public boolean conditions() {
		return this.damage > 0;
	}
}
