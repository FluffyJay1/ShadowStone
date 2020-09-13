package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class UnleashSharpenSword extends UnleashPower {
	public static final int ID = -10;
	public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Sharpen Sword",
			"<b> Unleash </b> an allied minion, then give it +1/+0/+0.", "res/unleashpower/sharpensword.png", CRAFT, 2,
			ID, Tooltip.UNLEASH);

	public UnleashSharpenSword(Board b) {
		super(b, TOOLTIP);
	}

	@Override
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		EffectStatChange e = new EffectStatChange("+1/+0/+0 from <b> Sharpen Sword. </b>", 1, 0, 0);
		e.change.setStat(EffectStats.ATTACK, 1);
		list.add(new EventAddEffect(m, e));
		return list;
	}

}
