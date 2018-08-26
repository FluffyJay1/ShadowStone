package cardpack.basic;

import java.util.LinkedList;

import server.Board;
import server.card.CardStatus;
import server.card.Minion;
import server.card.effect.*;
import server.event.*;

public class BellringerAngel extends Minion {
	public static final int ID = 6;

	public BellringerAngel(Board b, int team) {
		super(b, CardStatus.DECK, 2, 0, 0, 2, false, "Bellringer Angel",
				"<b> Ward. </b> \n <b> Last Words: </b> draw a card.", "res/card/basic/bellringerangel.png", team, ID);
		Effect e = new Effect(0, "<b> Ward. </b> \n <b> Last Words: </b> draw a card.") {
			@Override
			public LinkedList<Event> lastWords() {
				LinkedList<Event> list = new LinkedList<Event>();
				list.add(new EventDraw(this.owner.board.getPlayer(this.owner.team), 1));
				return list;
			}
		};
		e.set.setStat(EffectStats.WARD, 1);
		this.addBasicEffect(e);
	}
}
