package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.Card;
import server.card.Minion;
import server.card.effect.EffectStats;

public class EventRestore extends Event {
	public static final int ID = 13;
	int heal;
	Minion m;

	public EventRestore(Minion m, int heal) {
		super(ID);
		this.m = m;
		this.heal = heal;
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.m.health += heal;
		if (this.m.health > this.m.finalStatEffects.getStat(EffectStats.HEALTH)) {
			this.m.health = this.m.finalStatEffects.getStat(EffectStats.HEALTH);
		}
		// TODO on healed
	}

	@Override
	public String toString() {
		return this.id + " " + m.toReference() + heal + " ";
	}

	public static EventRestore fromString(Board b, StringTokenizer st) {
		Card m = Card.fromReference(b, st);
		int heal = Integer.parseInt(st.nextToken());
		return new EventRestore((Minion) m, heal);
	}

	@Override
	public boolean conditions() {
		return this.heal > 0;
	}
}
