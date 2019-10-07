package server.card;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;

public class Leader extends Minion {
	public Leader(Board b, ClassCraft craft, int id, String name) {
		super(b, new TooltipMinion(name, "", "res/leader/smile.png", craft, 0, 0, 0, 25, false, id, new Vector2f(),
				-1));
	}
}
