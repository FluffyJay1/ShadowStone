package server.card.cardpack.basic;

import client.tooltip.TooltipMinion;
import server.Board;
import server.card.CardStatus;
import server.card.ClassCraft;
import server.card.Minion;

public class Goblin extends Minion {
	public static final int ID = 1;
	public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Goblin", "", "res/card/basic/goblin.png", CRAFT, 1,
			1, 0, 2, true, ID);

	public Goblin(Board b) {
		super(b, TOOLTIP);
	}
}
