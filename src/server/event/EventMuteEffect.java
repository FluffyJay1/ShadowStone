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
	private boolean prevMute;
	private int prevHealth;

	public EventMuteEffect(Card c, Effect e, boolean mute) {
		super(ID, false);
		this.c = c;
		this.e = e;
		this.mute = mute;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		this.prevMute = this.e.mute;
		this.c.muteEffect(this.e, this.mute);
		if (c instanceof Minion) {
			Minion m = ((Minion) c);
			this.prevHealth = m.health;
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
		this.c.muteEffect(this.e, this.prevMute);
		if (c instanceof Minion) {
			Minion m = ((Minion) c);
			m.health = this.prevHealth;
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
