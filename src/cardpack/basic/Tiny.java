package cardpack.basic;

import java.util.LinkedList;

import server.Board;
import server.card.CardStatus;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStatChange;
import server.card.effect.EffectStats;
import server.event.Event;
import server.event.EventAddEffect;

public class Tiny extends Minion {
	public static final int ID = 4;

	public Tiny(Board b, int team) {
		super(b, CardStatus.DECK, 3, 2, 2, 3, "Tiny", "<b> Unleash: </b> Gain +2/+0/+2", "res/card/basic/tiny.png",
				team, ID);
		Effect e = new Effect(this, 0, "Unleash: Gain +2/+0/+2") {
			@Override
			public LinkedList<Event> unleash() {
				LinkedList<Event> list = new LinkedList<Event>();
				EffectStatChange ef = new EffectStatChange(this.owner, "Gained +2/+0/+2 from Unleash");
				ef.change.setStat(EffectStats.ATTACK_I, 2);
				ef.change.setStat(EffectStats.HEALTH_I, 2);
				list.add(new EventAddEffect(this.owner, ef));
				return list;
			}
		};
		this.addBasicEffect(e);
	}
}
