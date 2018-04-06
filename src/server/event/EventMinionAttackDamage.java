package server.event;

import java.util.LinkedList;

import server.card.Minion;

public class EventMinionAttackDamage extends Event {
    // damage phase of attack
    Minion m1, m2;

    public EventMinionAttackDamage(Minion m1, Minion m2) {
	this.m1 = m1;
	this.m2 = m2;
    }

    @Override
    public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
	if (!conditions()) {
	    return this.toString();
	}
	if (!loopprotection) {
	    eventlist.addAll(this.m2.takeDamage(m1.attack));
	    eventlist.addAll(this.m1.takeDamage(m2.attack));
	} else {
	    eventlist.add(new EventDamage(m1, m2.attack));
	    eventlist.add(new EventDamage(m2, m1.attack));
	}
	return this.toString();
    }

    @Override
    public boolean conditions() {
	return m1.alive && m2.alive;
    }

    @Override
    public String toString() {
	return "atkdmg " + m1.position + " " + m2.position + " " + conditions() + "\n";
    }
}
