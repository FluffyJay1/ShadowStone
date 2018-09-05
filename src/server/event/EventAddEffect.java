package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.Card;
import server.card.Minion;
import server.card.Target;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

public class EventAddEffect extends Event {
	public static final int ID = 1;
	public Target t;
	Effect e;

	public EventAddEffect(Target t, Effect e) {
		super(ID);
		this.t = t;
		this.e = e;
	}

	public EventAddEffect(Card c, Effect e) {
		super(ID);
		Target t = new Target(c);
		this.t = t;
		this.e = e;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		for (Card c : this.t.getTargets()) {
			Effect clonede = e.clone();
			c.addEffect(clonede);
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
			if (c.finalStatEffects.getUse(EffectStats.COUNTDOWN)
					&& c.finalStatEffects.getStat(EffectStats.COUNTDOWN) <= 0) {
				eventlist.add(new EventDestroy(c));
			}
		}
	}

	public String toString() {
		return this.id + " " + t.toString() + e.toString() + "\n";
	}

	public static EventAddEffect fromString(Board b, StringTokenizer st) {
		Target t = Target.fromString(b, st);
		Effect e = Effect.fromString(b, st);
		return new EventAddEffect(t, e);
	}

	public boolean conditions() {
		return true;
	}
}
