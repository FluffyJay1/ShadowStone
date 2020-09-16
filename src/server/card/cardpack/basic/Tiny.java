package server.card.cardpack.basic;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class Tiny extends Minion {
	public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Tiny",
			"<b> Unleash: </b> Gain +2/+0/+2 and <b> Rush. </b>", "res/card/basic/tiny.png", CRAFT, 3, 2, 2, 3, false,
			Tiny.class, new Vector2f(), -1, Tooltip.UNLEASH, Tooltip.RUSH);

	public Tiny(Board b) {
		super(b, TOOLTIP);
		Effect e = new Effect("Unleash: Gain +2/+0/+2 and Rush", false) {
			@Override
			public EventFlag unleash() {
				EventFlag ef = new EventFlag(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						EffectStatChange ef = new EffectStatChange("Gained +2/+0/+2 and <b> Rush </b> from Unleash", 2,
								0, 2);
						ef.set.setStat(EffectStats.RUSH, 1);
						eventlist.add(new EventAddEffect(this.effect.owner, ef));
					}
				};
				return ef;
			}
		};
		this.addEffect(true, e);
	}
}
