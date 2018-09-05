package cardpack.basic;

import java.util.LinkedList;

import server.Board;
import server.card.BoardObject;
import server.card.CardStatus;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStatChange;
import server.card.effect.EffectStats;
import server.event.*;

public class Tiny extends Minion {
	public static final int ID = 4;

	public Tiny(Board b, int team) {
		super(b, CardStatus.DECK, 3, 2, 2, 3, false, "Tiny", "<b> Unleash: </b> Gain +2/+0/+2 and <b> Rush. </b>",
				"res/card/basic/tiny.png", team, ID);
		Effect e = new Effect(0, "Unleash: Gain +2/+0/+2 and Rush") {
			@Override
			public EventFlag unleash() {
				EventFlag ef = new EventFlag(this) {
					@Override
					public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
						EffectStatChange ef = new EffectStatChange("Gained +2/+0/+2 and <b> Rush </b> from Unleash");
						ef.change.setStat(EffectStats.ATTACK, 2);
						ef.change.setStat(EffectStats.HEALTH, 2);
						ef.set.setStat(EffectStats.RUSH, 1);
						eventlist.add(new EventAddEffect(this.effect.owner, ef));
					}
				};
				return ef;
			}
		};
		this.addBasicEffect(e);
	}
}
