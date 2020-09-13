package server.card.cardpack.basic;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class WeatheredVanguard extends Minion {
	public static final int ID = 14;
	public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Weathered Vanguard",
			"<b> Battlecry: </b> Summon 2 <b> Knights. Unleash: </b> Give all allied minions +1/+0/+1.",
			"res/card/basic/weatheredvanguard.png", CRAFT, 6, 4, 2, 4, false, ID, new Vector2f(155, 120), 1.6,
			Tooltip.BATTLECRY, Knight.TOOLTIP, Tooltip.UNLEASH);

	public WeatheredVanguard(Board b) {
		super(b, TOOLTIP);
		Effect e = new Effect(TOOLTIP.description, true) {
			@Override
			public EventBattlecry battlecry() {
				EventBattlecry eb = new EventBattlecry(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						for (int i = 0; i < 2; i++) {
							eventlist.add(new EventCreateCard(new Knight(b), this.effect.owner.team, CardStatus.BOARD,
									this.effect.owner.cardpos + i * 2));
						}
					}
				};
				return eb;
			}

			@Override
			public EventFlag unleash() {
				EventFlag ef = new EventFlag(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						Board b = this.effect.owner.board;
						List<Minion> minions = b.getMinions(this.effect.owner.team, false, true);
						if (!minions.isEmpty()) {
							Effect stats = new EffectStatChange("+1/+0/+1 from Weathered Vanguard.", 1, 0, 1);
							eventlist.add(new EventAddEffect(minions, stats));
						}
					}
				};
				return ef;
			}
		};
		this.addEffect(true, e);
	}

}
