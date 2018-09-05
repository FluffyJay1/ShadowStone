package cardpack.basic;

import server.Board;
import server.card.CardStatus;
import server.card.Minion;

public class Fairy extends Minion {
	public static final int ID = 9;

	public Fairy(Board b, int team) {
		super(b, CardStatus.DECK, 1, 1, 1, 1, true, "Fairy", "", "res/card/basic/fairy.png", team, ID);
	}
}
