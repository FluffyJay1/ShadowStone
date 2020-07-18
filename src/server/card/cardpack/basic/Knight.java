package server.card.cardpack.basic;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;

public class Knight extends Minion {
	public static final int ID = 13;
	public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Knight", "", "res/card/basic/knight.png", CRAFT, 1,
			1, 1, 1, true, ID, new Vector2f(), -1);

	public Knight(Board b) {
		super(b, TOOLTIP);
	}
}
