package cardpack.basic;

import java.util.LinkedList;

import server.Board;
import server.card.Amulet;
import server.card.Card;
import server.card.CardStatus;
import server.card.Leader;
import server.card.Minion;
import server.card.Target;
import server.card.effect.Effect;
import server.card.effect.EffectStatChange;
import server.card.effect.EffectStats;
import server.event.Event;
import server.event.EventAddEffect;
import server.event.EventResolveTarget;

public class WellOfDestination extends Amulet {
	public static final int ID = 5;

	public WellOfDestination(Board b, int team) {
		super(b, CardStatus.DECK, 2, "Well of Destination",
				"At the start of your turn, give a random allied minion +1/+1/+1",
				"res/card/basic/wellofdestination.png", team, ID);
		Effect e = new Effect(0, "At the start of your turn, give a random allied minion +1/+1/+1") {
			@Override
			public LinkedList<Event> onTurnStart() {
				LinkedList<Event> list = new LinkedList<Event>();
				Target t = new Target(this, 1, "") {
					@Override
					public boolean canTarget(Card c) {
						return c.team == this.getCreator().owner.team && c instanceof Minion && !(c instanceof Leader)
								&& c.status.equals(CardStatus.BOARD);
					}

					@Override
					public void resolveTargets() {
						this.setRandomTarget();
					}
				};
				list.add(new EventResolveTarget(t));
				EffectStatChange e = new EffectStatChange("Gained +1/+1/+1 from Well of Destination");
				e.change.setStat(EffectStats.ATTACK, 1);
				e.change.setStat(EffectStats.MAGIC, 1);
				e.change.setStat(EffectStats.HEALTH, 1);
				list.add(new EventAddEffect(t, e));
				return list;
			}
		};
		this.addBasicEffect(e);
	}

}
