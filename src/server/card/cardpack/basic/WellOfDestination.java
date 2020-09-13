package server.card.cardpack.basic;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class WellOfDestination extends Amulet {
	public static final int ID = 5;
	public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
	public static final TooltipAmulet TOOLTIP = new TooltipAmulet("Well of Destination",
			"At the start of your turn, give a random allied minion +1/+1/+1.", "res/card/basic/wellofdestination.png",
			CRAFT, 2, ID);

	public WellOfDestination(Board b) {
		super(b, TOOLTIP);
		Effect e = new Effect("At the start of your turn, give a random allied minion +1/+1/+1", false) {
			@Override
			public EventFlag onTurnStart() {
				EventFlag ef = new EventFlag(this, true) {
					@Override
					public void resolve(List<Event> eventlist, boolean loopprotection) {
						Target t = new Target(this.effect, 1, "") {
							@Override
							public boolean canTarget(Card c) {
								return c.team == this.getCreator().owner.team && c instanceof Minion
										&& c.status.equals(CardStatus.BOARD);
							}

							@Override
							public void resolveTargets() {
								this.setRandomCards();
							}
						};
						t.resolveTargets();
						EffectStatChange e = new EffectStatChange("Gained +1/+1/+1 from Well of Destination", 1, 1, 1);
						eventlist.add(new EventAddEffect(t, e));
					}
				};
				return ef;
			}
		};
		this.addEffect(true, e);
	}

}
