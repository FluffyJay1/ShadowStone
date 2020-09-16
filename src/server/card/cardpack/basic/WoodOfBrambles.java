package server.card.cardpack.basic;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class WoodOfBrambles extends Amulet {
	public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
	public static final TooltipAmulet TOOLTIP = new TooltipAmulet("Wood of Brambles",
			"<b> Countdown(2). </b> \n <b> Battlecry: </b> add two <b> Faries </b> to your hand. Give all allied minions the following effect until this amulet leaves play: <b> Clash: </b> deal 1 damage to the enemy minion. \n Whenever an allied minion comes into play, give them that effect until this amulet leaves play.",
			"res/card/basic/woodofbrambles.png", CRAFT, 2, WoodOfBrambles.class, Tooltip.COUNTDOWN, Tooltip.BATTLECRY,
			Fairy.TOOLTIP, Tooltip.CLASH);

	public WoodOfBrambles(Board b) {
		super(b, TOOLTIP);
		Effect e = new Effect(TOOLTIP.description, true) {
			@Override
			public EventBattlecry battlecry() {
				EventBattlecry eb = new EventBattlecry(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						for (int i = 0; i < 2; i++) {
							eventlist.add(new EventCreateCard(new Fairy(this.effect.owner.board),
									this.effect.owner.team, CardStatus.HAND, 999));
						}
						Target t = new Target(this.effect, 10, "") {

							@Override
							public void resolveTargets() {
								for (BoardObject b : this.getCreator().owner.board
										.getBoardObjects(this.getCreator().owner.team, false, true, false)) {
									this.addCard(b);
								}
							}
						};
						t.resolveTargets();
						EffectBrambles e = new EffectBrambles(this.effect.owner);
						eventlist.add(new EventAddEffect(t, e));
					}
				};
				return eb;
			}

			@Override
			public EventFlag onListenEvent(Event event) {
				if (this.owner.status.equals(CardStatus.BOARD) && event instanceof EventEnterPlay) {
					EventEnterPlay e = (EventEnterPlay) event;
					if (e.c instanceof Minion && e.c.team == this.owner.team) {
						EventFlag ef = new EventFlag(this, false) {
							@Override
							public void resolve(List<Event> eventlist, boolean loopprotection) {
								eventlist.add(new EventAddEffect(e.c, new EffectBrambles(this.effect.owner)));
							}
						};
						return ef;
					}
				}
				return null;
			}
		};
		e.set.setStat(EffectStats.COUNTDOWN, 2);
		this.addEffect(true, e);
	}

	public static class EffectBrambles extends Effect {
		Card creator;

		public EffectBrambles(String description, boolean listener) {
			super(description, listener);
		}

		public EffectBrambles(Card creator) {
			this("Has <b> Clash: </b> deal 1 damage to the enemy minion until the corresponding Wood of Brambles leaves play.",
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
		public String extraStateString() {
			return this.creator.toReference();
		}

		@Override
		public Effect loadExtraState(Board b, StringTokenizer st) {
			this.creator = Card.fromReference(b, st);
			return this;
		}

	}
}
