package server.card.cardpack.basic;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class Puppet extends Minion {
	public static final int ID = 17;
	public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Puppet",
			"<b> Rush. </b> At the end of your opponent's turn, destroy this minion.", "res/card/basic/puppet.png",
			CRAFT, 0, 1, 0, 1, false, ID, new Vector2f(161, 143), 1.4, Tooltip.RUSH);

	public Puppet(Board b) {
		super(b, TOOLTIP);
		Effect e = new Effect(0, TOOLTIP.description, true) {
			@Override
			public EventFlag onListenEvent(Event e) {
				if (e instanceof EventTurnEnd && ((EventTurnEnd) e).p.team != this.owner.team
						&& this.owner.status.equals(CardStatus.BOARD)) {
					EventFlag ef = new EventFlag(this, false) {
						@Override
						public void resolve(List<Event> eventlist, boolean loopprotection) {
							eventlist.add(new EventDestroy(this.effect.owner));
						}
					};
					return ef;
				}
				return null;
			}
		};
		e.set.setStat(EffectStats.RUSH, 1);
		this.addEffect(true, e);
	}
}
