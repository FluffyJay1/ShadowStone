package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventMuteEffect extends Event {
	public static final int ID = 29;
	public Card c;
	Effect e;
	boolean mute;

	public EventMuteEffect(Card c, Effect e, boolean mute) {
		super(ID);
		this.c = c;
		this.e = e;
		this.mute = mute;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		this.c.muteEffect(this.e, this.mute);
		if (c instanceof Minion) {
			Minion m = ((Minion) c);
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
	public String toString() {
		return this.id + " " + this.c.toReference() + this.e.toReference() + this.mute + "\n";
	}

	public static EventMuteEffect fromString(Board b, StringTokenizer st) {
		Card c = Card.fromReference(b, st);
		Effect e = Effect.fromReference(b, st);
		boolean mute = Boolean.parseBoolean(st.nextToken());
		return new EventMuteEffect(c, e, mute);
	}

	@Override
	public boolean conditions() {
		return this.c.getAdditionalEffects().contains(this.e);
	}
}
