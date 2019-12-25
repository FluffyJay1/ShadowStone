package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class UnleashPower extends Card {

	public Player p;
	public int unleashesThisTurn = 0;

	public UnleashPower(Board b, TooltipUnleashPower tooltip) {
		super(b, tooltip);
		Effect e = new Effect(0, "", tooltip.cost);
		e.set.setStat(EffectStats.ATTACKS_PER_TURN, 1);
		this.addEffect(true, e);
	}

	// this returns a linkedlist event because fuck u
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		return list;
	}

}
