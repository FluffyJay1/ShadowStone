package server.event;

import java.util.*;

import server.*;
import server.card.*;

public class EventMinionAttack extends Event {
	// start attack
	public static final int ID = 8;
	public Minion m1, m2;

	public EventMinionAttack(Minion m1, Minion m2) {
		super(ID);
		this.m1 = m1;
		this.m2 = m2;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		if (!loopprotection) {
			m1.attacksThisTurn++;
			eventlist.addAll(m1.onAttack(m2));
			if (!(this.m2 instanceof Leader)) {
				eventlist.addAll(m1.clash(m2));
			}
			eventlist.addAll(m2.onAttacked(m1));
			if (!(this.m1 instanceof Leader)) {
				eventlist.addAll(m2.clash(m1));
			}

		}
		eventlist.add(new EventMinionAttackDamage(m1, m2));
	}

	@Override
	public String toString() {
		return this.id + " " + m1.toReference() + m2.toReference() + "\n";
	}

	public static EventMinionAttack fromString(Board b, StringTokenizer st) {
		Card m1 = Card.fromReference(b, st);
		Card m2 = Card.fromReference(b, st);
		return new EventMinionAttack((Minion) m1, (Minion) m2);
	}

	@Override
	public boolean conditions() {
		return m1.isInPlay() && m2.isInPlay();
	}
}
