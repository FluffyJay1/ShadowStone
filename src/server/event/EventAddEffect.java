package server.event;

import java.util.LinkedList;

import server.card.Card;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

public class EventAddEffect extends Event {
	Card c;
	Effect e;

	public EventAddEffect(Card c, Effect e) {
		this.c = c;
		this.e = e;
	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		if (!this.conditions()) {
			return this.toString();
		}
		this.c.addEffect(e);
		if (c instanceof Minion) {
			Minion m = ((Minion) c);
			if (e.set.use[EffectStats.HEALTH]) {
				m.health = e.set.stats[EffectStats.HEALTH];
			}
			if (e.change.use[EffectStats.HEALTH] && e.change.stats[EffectStats.HEALTH] > 0) {
				m.health += e.change.stats[EffectStats.HEALTH];
			}
			if (c.finalStatEffects.getStat(EffectStats.HEALTH) < m.health) {
				m.health = m.finalStatEffects.getStat(EffectStats.HEALTH);
			}
			if (m.health <= 0) {
				eventlist.add(new EventDestroy(m));
			}
		}
		return this.toString();
	}

	public String toString() {
		return "adde " + c.toString() + " " + e.toString();
	}

	public boolean conditions() {
		return true;
	}
}
