package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class UnleashImbueMagic extends UnleashPower {
	public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Imbue Magic",
			"Give an allied minion +0/+1/+0, then <b> Unleash </b> it.", "res/unleashpower/imbuemagic.png", CRAFT, 2,
			UnleashImbueMagic.class, Tooltip.UNLEASH);

	public UnleashImbueMagic(Board b) {
		super(b, TOOLTIP);
	}

	@Override
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		EffectStatChange e = new EffectStatChange("+0/+1/+0 from <b> Imbue Magic. </b>", 0, 1, 0);
		e.change.setStat(EffectStats.MAGIC, 1);
		list.add(new EventAddEffect(m, e));
		list.add(new EventUnleash(this, m));
		return list;
	}
}
