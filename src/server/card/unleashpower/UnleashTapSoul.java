package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class UnleashTapSoul extends UnleashPower {
	public static final int ID = -14;
	public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Tap Soul",
			" Deal 2 damage to your leader. <b> Unleash </b> an allied minion. If <b> Vengeance </b> is active for you, this can be used once more per turn.",
			"res/unleashpower/tapsoul.png", CRAFT, 1, ID, Tooltip.UNLEASH, Tooltip.VENGEANCE);

	public UnleashTapSoul(Board b) {
		super(b, TOOLTIP);
		Effect vengeanceBonus = new Effect("When Vengeance is active, this can be used once more per turn.", true) {
			boolean vengeance;

			@Override
			public EventFlag onListenEvent(Event e) {
				if (e instanceof EventDamage || e instanceof EventRestore || e instanceof EventAddEffect) {
					if (!vengeance && p.vengeance()) {
						vengeance = true;
						EventFlag ef = new EventFlag(this, false) {
							@Override
							public void resolve(List<Event> eventlist, boolean loopprotection) {
								Effect esc = new Effect();
								esc.change.setStat(EffectStats.ATTACKS_PER_TURN, 1);
								eventlist.add(new EventSetEffectStats(this.effect, esc));
							}
						};
						return ef;
					}
					if (vengeance && !p.vengeance()) {
						vengeance = false;
						EventFlag ef = new EventFlag(this, false) {
							@Override
							public void resolve(List<Event> eventlist, boolean loopprotection) {
								Effect esc = new Effect();
								esc.change.setStat(EffectStats.ATTACKS_PER_TURN, 0);
								eventlist.add(new EventSetEffectStats(this.effect, esc));
							}
						};
						return ef;
					}
				}
				return null;
			}

			@Override
			public String extraStateString() {
				return this.vengeance + " ";
			}

			@Override
			public Effect loadExtraState(Board b, StringTokenizer st) {
				this.vengeance = Boolean.parseBoolean(st.nextToken());
				return this;
			}
		};
		this.addEffect(true, vengeanceBonus);

	}

	@Override
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventDamage(this.p.leader, 2, false));
		list.add(new EventUnleash(this, m));
		return list;
	}
}
