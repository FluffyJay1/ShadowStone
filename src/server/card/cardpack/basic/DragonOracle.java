package server.card.cardpack.basic;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class DragonOracle extends Spell {
	public static final int ID = 15;
	public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
	public static final TooltipSpell TOOLTIP = new TooltipSpell("Dragon Oracle",
			"Gain one empty mana orb. If <b> Overflow </b> is active for you, draw a card.",
			"res/card/basic/dragonoracle.png", CRAFT, 2, ID);

	public DragonOracle(Board b) {
		super(b, TOOLTIP);
		Effect e = new Effect(TOOLTIP.description, false) {
			@Override
			public EventBattlecry battlecry() {
				EventBattlecry eb = new EventBattlecry(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						Player player = this.effect.owner.board.getPlayer(this.effect.owner.team);
						eventlist.add(new EventManaChange(player, 1, true, false));
						if (player.overflow()) {
							eventlist.add(new EventDraw(player));
						}
					}
				};
				return eb;
			}
		};
		this.addEffect(true, e);
	}
}
