package server.card;

import server.Board;

public class Leader extends Minion {
	public Leader(Board b, int team, int id, String name, String text) {
		super(b, CardStatus.BOARD, 0, 0, 0, 25, false, name, text, "res/leader/smile.png", team, id);
	}

	public String toString() {
		return "Leader " + name + " " + alive + " " + this.finalStatEffects.statsToString() + " " + this.health;
	}
}
