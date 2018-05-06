package server.event;

import java.util.LinkedList;

import server.card.Minion;

public class EventMinionDamage extends Event {
	// whenever a minion does damage from card text i.e. ragnaros end of turn
	// effect
	int damage;
	Minion m1, m2;

	public EventMinionDamage(Minion m1, Minion m2, int damage) {
		this.m1 = m1;
		this.m2 = m2;
		this.damage = damage;
	}

	@Override
	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		if (!conditions()) {
			return this.toString();
		}
		eventlist.add(new EventDamage(m2, this.damage));
		return this.toString();
	}

	@Override
	public boolean conditions() {
		return m1.alive && m2.alive;
	}

	@Override
	public String toString() {
		return "mdmg " + m1.boardpos + " " + m2.boardpos + " " + damage + " " + conditions() + "\n";
	}
}
