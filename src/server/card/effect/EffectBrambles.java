package server.card.effect;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

public class EffectBrambles extends Effect {
	public static final int ID = 1;
	Card creator;

	public EffectBrambles(Card creator) {
		super(ID,
				"Has <b> Clash: </b> deal 1 damage to the enemy minion until the corresponding Wood of Brambles leaves play.",
				true);
		this.creator = creator;
	}

	@Override
	public EventClash clash(Minion target) {
		EventClash ec = new EventClash(this, target, false) {
			@Override
			public void resolve(List<Event> eventlist, boolean loopprotection) {
				eventlist.add(new EventEffectDamage(this.effect, target, 1));
			}
		};
		return ec;
	}

	@Override
	public EventFlag onListenEvent(Event event) {
		if (event instanceof EventLeavePlay) {
			EventLeavePlay e = (EventLeavePlay) event;
			if (this.creator == e.c) {
				EventFlag ef = new EventFlag(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						eventlist.add(new EventRemoveEffect(this.effect.owner, this.effect));
					}
				};
				return ef;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.id + " " + this.creator.toReference();
	}

	public static EffectBrambles fromString(Board b, StringTokenizer st) {
		Card creator = Card.fromReference(b, st);
		return new EffectBrambles(creator);
	}

}
