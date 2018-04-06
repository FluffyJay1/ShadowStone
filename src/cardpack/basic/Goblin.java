package cardpack.basic;

import server.Board;
import server.card.Minion;

public class Goblin extends Minion {
    public static final int ID = 1;

    public Goblin(Board b) {
	super(b, 1, 1, 0, 2, "Goblin", "", "res/card/basic/goblin.png", ID);
    }
}
