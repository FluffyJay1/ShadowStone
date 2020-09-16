package server.card.cardpack.basic;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class PuppetRoom extends Amulet {
	public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;

	public static final TooltipAmulet TOOLTIP = new TooltipAmulet("Puppet Room",
			"<b> Countdown(3). Battlecry: </b> put a <b> Puppet </b> in your hand. At the end of your turn, put a <b> Puppet </b> in your hand.",
			"res/card/basic/puppetroom.png", CRAFT, 3, PuppetRoom.class, Tooltip.COUNTDOWN, Tooltip.BATTLECRY,
			Puppet.TOOLTIP);

	public PuppetRoom(Board b) {
		super(b, TOOLTIP);
		Effect e = new Effect(TOOLTIP.description, false) {
			@Override
			public EventBattlecry battlecry() {
				EventBattlecry eb = new EventBattlecry(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						Board b = this.effect.owner.board;
						eventlist
								.add(new EventCreateCard(new Puppet(b), this.effect.owner.team, CardStatus.HAND, 1000));
					}
				};
				return eb;
			}

			@Override
			public EventFlag onTurnEnd() {
				EventFlag ef = new EventFlag(this, false) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						Board b = this.effect.owner.board;
						eventlist
								.add(new EventCreateCard(new Puppet(b), this.effect.owner.team, CardStatus.HAND, 1000));
					}
				};
				return ef;
			}
		};
		e.set.setStat(EffectStats.COUNTDOWN, 3);
		this.addEffect(true, e);
	}
}
