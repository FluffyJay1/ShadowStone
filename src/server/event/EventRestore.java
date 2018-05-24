package server.event;

import java.util.LinkedList;

import server.card.Minion;
import server.card.effect.EffectStats;

public class EventRestore extends Event {
	int heal;
	Minion m;

	public EventRestore(Minion m, int heal) {
		this.m = m;
		this.heal = heal;
	}

	@Override
	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.m.health += heal;
		if (this.m.health > this.m.finalStatEffects.getEffectStat(EffectStats.HEALTH_I)) {
			this.m.health = this.m.finalStatEffects.getEffectStat(EffectStats.HEALTH_I);
		}
		// todo on healed
		return this.toString();
	}

	@Override
	public String toString() {
		return "restore " + m.boardpos + " " + heal + "\n";
	}

	@Override
	public boolean conditions() {
		return this.heal > 0;
	}
}
