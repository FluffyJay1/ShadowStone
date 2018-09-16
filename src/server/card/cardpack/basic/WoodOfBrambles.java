package server.card.cardpack.basic;

import java.util.LinkedList;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import server.Board;
import server.card.Amulet;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.ClassCraft;
import server.card.Leader;
import server.card.Minion;
import server.card.Target;
import server.card.effect.*;
import server.event.*;

public class WoodOfBrambles extends Amulet {
	public static final int ID = 8;
	public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
	public static final TooltipAmulet TOOLTIP = new TooltipAmulet("Wood of Brambles",
			"<b> Countdown(2). </b> \n <b> Battlecry: </b> add two <b> Faries </b> to your hand. Give all allied minions the following effect until this amulet leaves play: <b> Clash: </b> deal 1 damage to the enemy minion. \n Whenever an allied minion comes into play, give them that effect until this amulet leaves play.",
			CRAFT, 2, Tooltip.COUNTDOWN, Tooltip.BATTLECRY, Fairy.TOOLTIP, Tooltip.CLASH);

	public WoodOfBrambles(Board b, int team) {
		super(b, CardStatus.DECK, 2, TOOLTIP, "res/card/basic/woodofbrambles.png", team, CRAFT, ID);
		Effect e = new Effect(0, TOOLTIP.description) {
			@Override
			public EventBattlecry battlecry() {
				EventBattlecry eb = new EventBattlecry(this) {
					@Override
					public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
						for (int i = 0; i < 2; i++) {
							eventlist.add(new EventCreateCard(this.effect.owner.board,
									new Fairy(this.effect.owner.board, this.effect.owner.team), this.effect.owner.team,
									CardStatus.HAND, 999));
						}
						Target t = new Target(this.effect, 5, "") {

							@Override
							public void resolveTargets() {
								for (BoardObject b : this.getCreator().owner.board
										.getBoardObjects(this.getCreator().owner.team, true, false, true)) {
									this.setTarget(b);
								}
							}
						};
						eventlist.add(new EventResolveTarget(t));
						EffectBrambles e = new EffectBrambles(this.effect.owner);
						eventlist.add(new EventAddEffect(t, e));
					}
				};
				return eb;
			}

			@Override
			public EventFlag onEvent(Event event) {
				if (this.owner.status.equals(CardStatus.BOARD) && event instanceof EventEnterPlay) {
					EventEnterPlay e = (EventEnterPlay) event;
					if (e.c instanceof Minion && e.c.team == this.owner.team) {
						EventFlag ef = new EventFlag(this) {
							@Override
							public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
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
		this.addBasicEffect(e);
	}
}
