package server.card;

import client.tooltip.*;
import server.Board;

public class Leader extends Minion {
	public Leader(Board b, ClassCraft craft, int id, String name) {
		super(b, new TooltipMinion(name, "", "res/leader/smile.png", craft, 0, 0, 0, 25, false, id));
	}

	public String toString() {
		return "Leader " + this.tooltip.name + " " + alive + " " + this.finalStatEffects.statsToString() + " "
				+ this.health;
	}
}
