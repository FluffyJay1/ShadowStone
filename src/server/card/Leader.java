package server.card;

import client.tooltip.*;
import server.Board;

public class Leader extends Minion {
	public Leader(Board b, int team, int id, String name) {
		super(b, CardStatus.BOARD, 0, 0, 0, 25, false, new TooltipMinion(name, "", 0, 0, 0, 25, false),
				"res/leader/smile.png", team, id);
	}

	public String toString() {
		return "Leader " + this.tooltip.name + " " + alive + " " + this.finalStatEffects.statsToString() + " "
				+ this.health;
	}
}
