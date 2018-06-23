package cardpack.basic;

import server.Board;
import server.card.CardStatus;
import server.card.Minion;

public class Goblin extends Minion {
	public static final int ID = 1;

	public Goblin(Board b, int team) {
		super(b, CardStatus.DECK, 1, 1, 0, 2, true, "Goblin", "", "res/card/basic/goblin.png", team, ID);
	}
}
