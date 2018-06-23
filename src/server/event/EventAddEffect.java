package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.Card;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

public class EventAddEffect extends Event {
	public static final int ID = 1;
	Card c;
	Effect e;

	public EventAddEffect(Card c, Effect e) {
		super(ID);
		this.c = c;
		this.e = e;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
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
	}

	public String toString() {
		return this.id + " " + c.toReference() + e.toString();
	}

	public static EventAddEffect fromString(Board b, StringTokenizer st) {
		Card c = Card.fromReference(b, st);
		Effect e = Effect.fromString(b, st);
		return new EventAddEffect(c, e);
	}

	public boolean conditions() {
		return true;
	}
}
