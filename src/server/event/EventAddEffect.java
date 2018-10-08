package server.event;

import java.util.ArrayList;
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
	public ArrayList<Card> c;
	Effect e;

	public EventAddEffect(Target t, Effect e) {
		super(ID);
		this.c = new ArrayList<Card>();
		this.c.addAll(t.getTargets());
		this.e = e;
	}

	public EventAddEffect(ArrayList<Card> c, Effect e) {
		super(ID);
		this.c = new ArrayList<Card>();
		this.c.addAll(c);
		this.e = e;
	}

	public EventAddEffect(Card c, Effect e) {
		super(ID);
		this.c = new ArrayList<Card>();
		this.c.add(c);
		this.e = e;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		for (Card c : this.c) {
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
		String ret = this.id + " " + this.c.size() + " " + e.toString();
		for (int i = 0; i < this.c.size(); i++) {
			ret += this.c.get(i).toReference();
		}
		return ret + "\n";
	}

	public static EventAddEffect fromString(Board b, StringTokenizer st) {
		int size = Integer.parseInt(st.nextToken());
		Effect e = Effect.fromString(b, st);
		ArrayList<Card> c = new ArrayList<Card>();
		for (int i = 0; i < size; i++) {
			Card card = Card.fromReference(b, st);
			c.add(card);
		}
		return new EventAddEffect(c, e);
	}

	public boolean conditions() {
		return !this.c.isEmpty() && this.e != null;
	}
}
