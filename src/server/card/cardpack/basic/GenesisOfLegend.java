package server.card.cardpack.basic;

import java.util.LinkedList;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import server.Board;
import server.card.Amulet;
import server.card.Card;
import server.card.CardStatus;
import server.card.ClassCraft;
import server.card.Leader;
import server.card.Minion;
import server.card.Target;
import server.card.effect.Effect;
import server.card.effect.EffectStatChange;
import server.card.effect.EffectStats;
import server.event.*;

public class GenesisOfLegend extends Amulet {
	public static final int ID = 7;
	public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;

	public static final TooltipAmulet TOOLTIP = new TooltipAmulet("Gensis of Legend",
			"<b> Countdown(3). </b> At the end of your turn, give a random allied minion +0/+0/+1 and <b> Bane. </b>",
			"res/card/basic/genesisoflegend.png", CRAFT, 2, ID, Tooltip.COUNTDOWN, Tooltip.BANE);

	public GenesisOfLegend(Board b) {
		super(b, TOOLTIP);
		Effect e = new Effect(0, TOOLTIP.description) {
			@Override
			public EventFlag onTurnEnd() {
				EventFlag ef = new EventFlag(this) {
					@Override
					public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
						Target t = new Target(this.effect, 1, "") {
							@Override
							public boolean canTarget(Card c) {
								return c.team == this.getCreator().owner.team && c instanceof Minion
										&& !(c instanceof Leader) && c.status.equals(CardStatus.BOARD);
							}

							@Override
							public void resolveTargets() {
								this.setRandomTarget();
							}
						};
						t.resolveTargets();
						EffectStatChange e = new EffectStatChange(
								"Gained +0/+0/+1 and <b> Bane </b> from Genesis of Legend.");
						e.change.setStat(EffectStats.HEALTH, 1);
						e.set.setStat(EffectStats.BANE, 1);
						eventlist.add(new EventAddEffect(t, e));
					}
				};
				return ef;
			}
		};
		e.set.setStat(EffectStats.COUNTDOWN, 3);
		this.addBasicEffect(e);
	}
}
