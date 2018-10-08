package server.card.cardpack.basic;

import java.util.LinkedList;

import client.tooltip.*;
import server.Board;
import server.card.CardStatus;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.*;
import server.event.*;

public class BellringerAngel extends Minion {
	public static final int ID = 6;
	public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Bellringer Angel",
			"<b> Ward. </b> \n <b> Last Words: </b> draw a card.", "res/card/basic/bellringerangel.png", CRAFT, 2, 0, 0,
			2, false, ID, Tooltip.WARD, Tooltip.LASTWORDS);

	public BellringerAngel(Board b) {
		super(b, TOOLTIP);
		Effect e = new Effect(0, "<b> Ward. </b> \n <b> Last Words: </b> draw a card.") {
			@Override
			public EventLastWords lastWords() {
				EventLastWords lw = new EventLastWords(this) {
					@Override
					public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
						eventlist.add(new EventDraw(this.effect.owner.board.getPlayer(this.effect.owner.team), 1));
					}
				};
				return lw;
			}
		};
		e.set.setStat(EffectStats.WARD, 1);
		this.addBasicEffect(e);
	}
}
