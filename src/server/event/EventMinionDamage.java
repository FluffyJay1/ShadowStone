package server.event;

import java.util.LinkedList;

import server.card.minion.Minion;

public class EventMinionDamage extends Event {
    // whenever a minion does damage from card text i.e. ragnaros end of turn effect
    int damage;
    Minion m1, m2;

    public EventMinionDamage(Minion m1, Minion m2, int damage) {
	this.m1 = m1;
	this.m2 = m2;
	this.damage = damage;
    }

    @Override
    public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
	if (!loopprotection) {
	    eventlist.addAll(this.m2.takeDamage(this.damage));
	} else {
	    eventlist.add(new EventDamage(m2, this.damage));
	}
    }

    @Override
    public String toString() {
	return "mdmg " + m1.position + " " + m2.position + " " + damage;
    }
}
