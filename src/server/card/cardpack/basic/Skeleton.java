package server.card.cardpack.basic;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import server.Board;
import server.card.CardStatus;
import server.card.ClassCraft;
import server.card.Minion;

public class Skeleton extends Minion {
	public static final int ID = 10;
	public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Skeleton", "", "res/card/basic/skeleton.png", CRAFT,
			1, 1, 1, 1, true, ID);

	public Skeleton(Board b) {
		super(b, TOOLTIP);
	}
}
