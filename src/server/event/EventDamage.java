package server.event;

import java.util.LinkedList;

import server.card.minion.Minion;

public class EventDamage extends Event {
    //whenever damage is dealt
    int damage;
    Minion m;
    public EventDamage(Minion m, int damage) {
	this.m = m;
	this.damage = damage;
    }
    @Override
    public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
	this.m.health -= damage;
	if(m.health <= 0) {
	    eventlist.add(new EventDestroy(m));
	}
    }
    @Override
    public String toString() {
	return "dmg " + m.position + " " + damage;
    }
}
