package server.card.cardpack.basic;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;

public class Goblin extends Minion {
	public static final int ID = 1;
	public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Goblin", "", "res/card/basic/goblin.png", CRAFT, 1,
			1, 0, 2, true, ID, new Vector2f(), -1);

	public Goblin(Board b) {
		super(b, TOOLTIP);
	}
}
