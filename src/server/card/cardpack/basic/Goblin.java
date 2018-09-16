package server.card.cardpack.basic;

import client.tooltip.TooltipMinion;
import server.Board;
import server.card.CardStatus;
import server.card.ClassCraft;
import server.card.Minion;

public class Goblin extends Minion {
	public static final int ID = 1;
	public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Goblin", "", CRAFT, 1, 1, 0, 2, true);

	public Goblin(Board b, int team) {
		super(b, CardStatus.DECK, 1, 1, 0, 2, true, TOOLTIP, "res/card/basic/goblin.png", team, CRAFT, ID);
	}
}
