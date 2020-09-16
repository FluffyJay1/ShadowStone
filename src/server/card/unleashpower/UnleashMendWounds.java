package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class UnleashMendWounds extends UnleashPower {
	public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Mend Wounds",
			"<b> Unleash </b> an allied minion. Give it +0/+0/+1, then restore 1 health to it.",
			"res/unleashpower/mendwounds.png", CRAFT, 2, UnleashMendWounds.class, Tooltip.UNLEASH);

	public UnleashMendWounds(Board b) {
		super(b, TOOLTIP);
	}

	@Override
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		EffectStatChange e = new EffectStatChange("+0/+0/+1 from <b> Mend Wounds. </b>", 0, 0, 1);
		e.change.setStat(EffectStats.HEALTH, 1);
		list.add(new EventAddEffect(m, e));
		list.add(new EventRestore(m, 1));
		return list;
	}
}
