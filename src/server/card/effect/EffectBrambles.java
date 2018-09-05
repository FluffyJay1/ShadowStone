package server.card.effect;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.Card;
import server.card.Minion;
import server.card.Target;
import server.event.*;

public class EffectBrambles extends Effect {
	public static final int ID = 1;
	Card creator;

	public EffectBrambles(Card creator) {
		super(ID,
				"Has <b> Clash: </b> deal 1 damage to the enemy minion until the corresponding Wood of Brambles leaves play.");
		this.creator = creator;
	}

	@Override
	public EventClash clash(Minion target) {
		Target t = new Target(target);
		EventClash ec = new EventClash(this, t) {
			@Override
			public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
				eventlist.add(new EventMinionDamage((Minion) this.effect.owner, this.t, 1));
			}
		};
		return ec;
	}

	@Override
	public EventFlag onEvent(Event event) {
		if (event instanceof EventLeavePlay) {
			EventLeavePlay e = (EventLeavePlay) event;
			if (this.creator == e.c) {
				EventFlag ef = new EventFlag(this) {
					@Override
					public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
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

	@Override
	public EffectBrambles clone() {
		return new EffectBrambles(this.creator);
	}
}
