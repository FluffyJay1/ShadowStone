package server.event;

import java.util.LinkedList;

import server.card.Minion;

public class EventMinionAttack extends Event {
	// start attack
	Minion m1, m2;

	public EventMinionAttack(Minion m1, Minion m2) {
		this.m1 = m1;
		this.m2 = m2;
	}

	@Override
	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		if (!conditions()) {
			return this.toString();
		}
		String eventstring = this.toString();
		if (!loopprotection) {
			eventstring += Event.resolveAll(m1.onAttack(m2), loopprotection);
			eventstring += Event.resolveAll(m1.clash(m2), loopprotection);
			eventstring += Event.resolveAll(m2.onAttacked(m1), loopprotection);
			eventstring += Event.resolveAll(m2.clash(m1), loopprotection);
			/*
			 * eventlist.addAll(m1.onAttack(m2));
			 * eventlist.addAll(m1.clash(m2));
			 * eventlist.addAll(m2.onAttacked(m1));
			 * eventlist.addAll(m2.clash(m1));
			 */
		}
		eventlist.add(new EventMinionAttackDamage(m1, m2));
		return eventstring;
	}

	@Override
	public boolean conditions() {
		return m1.alive && m2.alive;
	}

	@Override
	public String toString() {
		return "atk " + m1.boardpos + " " + m2.boardpos + " " + conditions() + "\n";
	}
}
