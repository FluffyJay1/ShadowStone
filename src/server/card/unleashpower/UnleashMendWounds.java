package server.card.unleashpower;

import java.util.LinkedList;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipUnleashPower;
import server.Board;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStatChange;
import server.card.effect.EffectStats;
import server.event.*;

public class UnleashMendWounds extends UnleashPower {
	public static final int ID = -15;
	public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Mend Wounds",
			"<b> Unleash </b> an allied minion. Give it +0/+0/+1, then restore 1 health to it.",
			"res/unleashpower/mendwounds.png", CRAFT, 2, ID, Tooltip.UNLEASH);

	public UnleashMendWounds(Board b) {
		super(b, TOOLTIP, new Vector2f(665, 535), 0.6);
	}

	@Override
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		EffectStatChange e = new EffectStatChange("+0/+0/+1 from <b> Mend Wounds. </b>");
		e.change.setStat(EffectStats.HEALTH, 1);
		list.add(new EventAddEffect(m, e));
		list.add(new EventRestore(m, 1));
		return list;
	}
}
