package server.card.cardpack.basic;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;

public class Fighter extends Minion {
	public static final int ID = 2;
	public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Fighter", "", "res/card/basic/fighter.png", CRAFT, 2,
			2, 1, 2, true, ID, new Vector2f(), -1);

	public Fighter(Board b) {
		super(b, TOOLTIP);
	}
}
