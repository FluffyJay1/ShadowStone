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
			if (e.set.use[EffectStats.HEALTH_I]) {
				m.health = e.set.stats[EffectStats.HEALTH_I];
			}
			if (e.change.use[EffectStats.HEALTH_I] && e.change.stats[EffectStats.HEALTH_I] > 0) {
				m.health += e.change.stats[EffectStats.HEALTH_I];
			}
			if (c.finalStatEffects.getEffectStat(EffectStats.HEALTH_I) < m.health) {
				m.health = m.finalStatEffects.getEffectStat(EffectStats.HEALTH_I);
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
