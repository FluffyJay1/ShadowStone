package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventRemoveEffect extends Event {
	public static final int ID = 22;
	public Card c;
	Effect e;
	private int prevPos;
	private int oldHealth;

	public EventRemoveEffect(Card c, Effect e) {
		super(ID, false);
		this.c = c;
		this.e = e;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		this.prevPos = this.e.pos;
		this.c.removeEffect(this.e);
		if (c instanceof Minion) {
			Minion m = ((Minion) c);
			this.oldHealth = m.health;
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

	@Override
	public void undo() {
		this.c.addEffect(false, this.prevPos, this.e);
		if (this.c instanceof Minion) {
			Minion m = (Minion) this.c;
			m.health = this.oldHealth;
		}
	}

	@Override
	public String toString() {
		return this.id + " " + this.c.toReference() + this.e.toReference() + "\n";
	}

	public static EventRemoveEffect fromString(Board b, StringTokenizer st) {
		Card c = Card.fromReference(b, st);
		Effect e = Effect.fromReference(b, st);
		return new EventRemoveEffect(c, e);
	}

	@Override
	public boolean conditions() {
		return this.c.getEffects(false).contains(this.e);
	}
}
