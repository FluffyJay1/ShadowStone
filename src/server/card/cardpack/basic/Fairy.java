package server.card.cardpack.basic;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import server.Board;
import server.card.CardStatus;
import server.card.ClassCraft;
import server.card.Minion;

public class Fairy extends Minion {
	public static final int ID = 9;
	public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Fairy", "", "res/card/basic/fairy.png", CRAFT, 1, 1,
			0, 1, true, ID);

	public Fairy(Board b, int team) {
		super(b, team, TOOLTIP);
	}
}
