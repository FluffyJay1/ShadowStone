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
import server.event.EventUnleash;

public class UnleashImbueMagic extends UnleashPower {
	public static final int ID = -11;
	public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Imbue Magic",
			"Give a minion +0/+1/+0, then <b> Unleash </b> it.", "res/unleashpower/imbuemagic.png", CRAFT, 2, ID,
			Tooltip.UNLEASH);

	public UnleashImbueMagic(Board b, int team) {
		super(b, team, TOOLTIP, new Vector2f(393, 733), 0.4);
		Effect e = new Effect(0, "", 2);
		e.set.setStat(EffectStats.ATTACKS_PER_TURN, 1);
		this.addBasicEffect(e);
	}

	@Override
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		EffectStatChange e = new EffectStatChange("+0/+1/+0 from <b> Imbue Magic. </b>");
		e.change.setStat(EffectStats.MAGIC, 1);
		list.add(new EventAddEffect(m, e));
		list.add(new EventUnleash(this, m));
		return list;
	}
}
