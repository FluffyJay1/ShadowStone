package cardpack.basic;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import server.Board;
import server.card.CardStatus;
import server.card.Minion;

public class Fairy extends Minion {
	public static final int ID = 9;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Fairy", "", 1, 1, 0, 1, true);

	public Fairy(Board b, int team) {
		super(b, CardStatus.DECK, 1, 1, 0, 1, true, TOOLTIP, "res/card/basic/fairy.png", team, ID);
	}
}
