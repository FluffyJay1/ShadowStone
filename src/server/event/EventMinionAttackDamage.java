package server.event;

import java.util.LinkedList;

import server.card.minion.Minion;

public class EventMinionAttackDamage extends Event {
    // damage phase of attack
    Minion m1, m2;

    public EventMinionAttackDamage(Minion m1, Minion m2) {
	this.m1 = m1;
	this.m2 = m2;
    }

    @Override
    public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
	if (!m1.alive || !m2.alive) {
	    return;
	}
	if (!loopprotection) {
	    eventlist.addAll(this.m2.takeDamage(m1.attack));
	    eventlist.addAll(this.m1.takeDamage(m2.attack));
	} else {
	    eventlist.add(new EventDamage(m2, m1.attack));
	    eventlist.add(new EventDamage(m2, m1.attack));
	}
    }

    @Override
    public String toString() {
	return "atkdmg " + m1.position + " " + m2.position;
    }
}
