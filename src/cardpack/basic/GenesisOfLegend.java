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

public class GenesisOfLegend extends Amulet {
	public static final int ID = 7;

	public GenesisOfLegend(Board b, int team) {
		super(b, CardStatus.DECK, 2, "Gensis of Legend",
				"<b> Countdown(3). </b> At the end of your turn, give a random allied minion +0/+0/+1 <b> Bane. </b>",
				"res/card/basic/genesisoflegend.png", team, ID);
		Effect e = new Effect(0, "At the end of your turn, give a random allied minion +0/+0/+1 <b> Bane. </b>") {
			@Override
			public LinkedList<Event> onTurnEnd() {
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
				EffectStatChange e = new EffectStatChange("Gained +0/+0/+1 and <b> Bane </b> from Genesis of Legend.");
				e.change.setStat(EffectStats.HEALTH, 1);
				e.set.setStat(EffectStats.BANE, 1);
				list.add(new EventAddEffect(t, e));
				return list;
			}
		};
		e.set.setStat(EffectStats.COUNTDOWN, 3);
		this.addBasicEffect(e);
	}
}
