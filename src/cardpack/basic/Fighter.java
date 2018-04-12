package cardpack.basic;

import server.Board;
import server.card.CardStatus;
import server.card.Minion;

public class Fighter extends Minion {
	public static final int ID = 2;

	public Fighter(Board b) {
		super(b, CardStatus.DECK, 2, 2, 1, 2, "Fighter", "", "res/card/basic/fighter.png", ID);
	}
}
