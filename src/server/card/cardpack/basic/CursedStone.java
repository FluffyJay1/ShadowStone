package server.card.cardpack.basic;

import java.util.*;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.event.misc.*;

public class CursedStone extends Minion {
	public static final int ID = 12;
	public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Cursed Stone",
			"<b> Unleash: Blast(X) </b> and gain <b> Last Words: </b> Deal X damage to a random allied minion. X equals the amount of health your leader is missing.",
			"res/card/basic/cursedstone.png", CRAFT, 5, 1, 5, 5, false, ID, new Vector2f(), -1, Tooltip.UNLEASH,
			Tooltip.BLAST, Tooltip.LASTWORDS);

	public CursedStone(Board b) {
		super(b, TOOLTIP);
		/*
		 * it's called cursed stone not because of the stone itself, but because of the
		 * anonymous classes
		 */
		Effect e = new Effect(TOOLTIP.description, false) {
			@Override
			public EventFlag unleash() {
				EventFlag ef = new EventFlag(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						Player player = this.effect.owner.board.getPlayer(this.effect.owner.team);
						int missing = player.leader.finalStatEffects.getStat(EffectStats.HEALTH) - player.leader.health;
						eventlist.add(new EventBlast(this.effect, missing));
						Effect lw = new EffectLastWordsAlliedBlast(missing);
						eventlist.add(new EventAddEffect(this.effect.owner, lw));
					}
				};
				return ef;
			}
		};
		this.addEffect(true, e);
	}

}
