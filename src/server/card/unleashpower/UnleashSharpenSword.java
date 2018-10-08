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
import server.event.Event;
import server.event.EventAddEffect;
import server.event.EventFlag;
import server.event.EventUnleash;

public class UnleashSharpenSword extends UnleashPower {
	public static final int ID = -10;
	public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Sharpen Sword",
			"<b> Unleash </b> an allied minion, then give it +1/+0/+0.", "res/unleashpower/sharpensword.png", CRAFT, 2,
			ID, Tooltip.UNLEASH);

	public UnleashSharpenSword(Board b) {
		super(b, TOOLTIP, new Vector2f(500, 330), 0.3);
	}

	@Override
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		EffectStatChange e = new EffectStatChange("+1/+0/+0 from <b> Sharpen Sword. </b>");
		e.change.setStat(EffectStats.ATTACK, 1);
		list.add(new EventAddEffect(m, e));
		return list;
	}

}
