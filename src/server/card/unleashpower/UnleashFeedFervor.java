package server.card.unleashpower;

import java.util.*;

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

	public UnleashFeedFervor(Board b) {
		super(b, TOOLTIP);
		Effect overflowDiscount = new Effect("When Overflow is active, this costs 1 less", true) {
			boolean overflow;

			@Override
			public EventFlag onListenEvent(Event e) {
				if (e instanceof EventManaChange) {
					if (!overflow && p.overflow()) {
						overflow = true;
						EventFlag ef = new EventFlag(this, false) {
							@Override
							public void resolve(List<Event> eventlist, boolean loopprotection) {
								Effect esc = new Effect();
								esc.change.setStat(EffectStats.COST, -1);
								eventlist.add(new EventSetEffectStats(this.effect, esc)); // WHY
							}
						};
						return ef;
					}
					if (overflow && !p.overflow()) {
						overflow = false;
						EventFlag ef = new EventFlag(this, false) {
							@Override
							public void resolve(List<Event> eventlist, boolean loopprotection) {
								Effect esc = new Effect();
								esc.change.setStat(EffectStats.COST, 0);
								eventlist.add(new EventSetEffectStats(this.effect, esc)); // WHY
							}
						};
						return ef;
					}
				}
				return null;
			}

			@Override
			public String extraStateString() {
				return this.overflow + " ";
			}

			@Override
			public Effect loadExtraState(Board b, StringTokenizer st) {
				this.overflow = Boolean.parseBoolean(st.nextToken());
				return this;
			}
		};
		this.addEffect(true, overflowDiscount);
	}
}
