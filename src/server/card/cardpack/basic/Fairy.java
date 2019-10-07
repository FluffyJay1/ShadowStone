package server.card.cardpack.basic;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;

public class Fairy extends Minion {
	public static final int ID = 9;
	public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Fairy", "", "res/card/basic/fairy.png", CRAFT, 1, 1,
			1, 1, true, ID, new Vector2f(), -1);

	public Fairy(Board b) {
		super(b, TOOLTIP);
	}
}
