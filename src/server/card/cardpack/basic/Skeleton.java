package server.card.cardpack.basic;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;

public class Skeleton extends Minion {
	public static final int ID = 10;
	public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
	public static final TooltipMinion TOOLTIP = new TooltipMinion("Skeleton", "", "res/card/basic/skeleton.png", CRAFT,
			1, 1, 1, 1, true, ID, new Vector2f(), -1);

	public Skeleton(Board b) {
		super(b, TOOLTIP);
	}
}
