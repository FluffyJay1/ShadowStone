package server.card.unleashpower;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class UnleashFeedFervor extends UnleashPower {
	public static final int ID = -12;
	public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Feed Fervor",
			"<b> Unleash </b> an allied minion. If <b> Overflow </b> is active for you, this costs 1 less.",
			"res/unleashpower/feedfervor.png", CRAFT, 2, ID, Tooltip.UNLEASH, Tooltip.OVERFLOW);

	Effect overflowDiscount;

	public UnleashFeedFervor(Board b) {
		super(b, TOOLTIP, new Vector2f(410, 200), 0.3);
		this.overflowDiscount = new Effect(0, "When Overflow is active, this costs 1 less") {
			boolean overflow;

			@Override
			public EventFlag onEvent(Event e) {
				if (e instanceof EventManaChange) {
					if (!overflow && p.overflow()) {
						overflow = true;
						EventFlag ef = new EventFlag(this) {
							@Override
							public void resolve(List<Event> eventlist, boolean loopprotection) {
								EffectStatChange esc = new EffectStatChange("");
								esc.change.setStat(EffectStats.COST, -1);
								eventlist.add(new EventSetEffectStats(overflowDiscount, esc)); // WHY
							}
						};
						return ef;
					}
					if (overflow && !p.overflow()) {
						overflow = false;
						EventFlag ef = new EventFlag(this) {
							@Override
							public void resolve(List<Event> eventlist, boolean loopprotection) {
								EffectStatChange esc = new EffectStatChange("");
								esc.change.setStat(EffectStats.COST, 0);
								eventlist.add(new EventSetEffectStats(overflowDiscount, esc)); // WHY
							}
						};
						return ef;
					}
				}
				return null;
			}
		};
		this.addBasicEffect(this.overflowDiscount);

	}
}
