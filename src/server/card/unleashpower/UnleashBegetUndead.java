package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.cardpack.basic.*;
import server.card.effect.*;
import server.event.*;

public class UnleashBegetUndead extends UnleashPower {
	public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
	public static final TooltipUnleashPower TOOLTIP = new TooltipUnleashPower("Beget Undead",
			"<b> Unleash </b> an allied minion. Give it <b> Last Words: </b> summon a <b> Skeleton. </b> Then deal 1 damage to it.",
			"res/unleashpower/begetundead.png", CRAFT, 2, UnleashBegetUndead.class, Tooltip.UNLEASH, Tooltip.LASTWORDS,
			Skeleton.TOOLTIP);

	public UnleashBegetUndead(Board b) {
		super(b, TOOLTIP);

	}

	@Override
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		EffectLastWordsSummon elws = new EffectLastWordsSummon("Summons a <b> Skeleton </b> upon death.",
				Skeleton.class, this.p.team);
		list.add(new EventAddEffect(m, elws));
		list.add(new EventDamage(m, 1, this.finalStatEffects.getStat(EffectStats.POISONOUS) > 0));
		return list;
	}
}
