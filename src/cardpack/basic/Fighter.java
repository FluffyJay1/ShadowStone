package cardpack.basic;

import client.tooltip.TooltipMinion;
import server.Board;
import server.card.CardStatus;
import server.card.Minion;

public class Fighter extends Minion {
	public static final int ID = 2;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Fighter", "", 2, 2, 1, 2, true);

	public Fighter(Board b, int team) {
		super(b, CardStatus.DECK, 2, 2, 1, 2, true, TOOLTIP, "res/card/basic/fighter.png", team, ID);
	}
}
