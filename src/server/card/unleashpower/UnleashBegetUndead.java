package server.card.unleashpower;

import java.util.LinkedList;

import org.newdawn.slick.geom.Vector2f;

import client.tooltip.Tooltip;
import client.tooltip.TooltipUnleashPower;
import server.Board;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.cardpack.basic.Skeleton;
import server.card.effect.*;
import server.event.*;

public class UnleashBegetUndead extends UnleashPower {
	public static final int ID = -13;
	public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Beget Undead",
			"<b> Unleash </b> an allied minion. Give it <b> Last Words: </b> summon a <b> Skeleton. </b> Then deal 1 damage to it.",
			"res/unleashpower/begetundead.png", CRAFT, 2, ID, Tooltip.UNLEASH, Tooltip.LASTWORDS, Skeleton.TOOLTIP);

	public UnleashBegetUndead(Board b) {
		super(b, TOOLTIP, new Vector2f(410, 460), 0.6);

	}

	@Override
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		EffectLastWordsSummon elws = new EffectLastWordsSummon("Summons a <b> Skeleton </b> upon death.", Skeleton.ID,
				this.p.team);
		list.add(new EventAddEffect(m, elws));
		list.add(new EventDamage(m, 1, this.finalStatEffects.getStat(EffectStats.POISONOUS) > 0));
		return list;
	}
}
