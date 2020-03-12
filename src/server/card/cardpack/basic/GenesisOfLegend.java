package server.card.cardpack.basic;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
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
				EventFlag ef = new EventFlag(this, true) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						Target t = new Target(this.effect, 1, "") {
							@Override
							public boolean canTarget(Card c) {
								return c.team == this.getCreator().owner.team && c instanceof Minion
										&& c.status.equals(CardStatus.BOARD);
							}

							@Override
							public void resolveTargets() {
								this.setRandomCards();
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
		this.addEffect(true, e);
	}
}
