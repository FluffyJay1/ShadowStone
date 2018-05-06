package server.card;

import server.Board;

public class Leader extends Minion {
	public Leader(Board b, String name, String text, int team) {
		super(b, CardStatus.BOARD, 0, 0, 0, 25, name, text, "res/leader/smile.png", 0);
		this.team = team;
		this.boardpos = team > 0 ? 1 : -1;
	}

	public String toString() {
		return "Leader " + name + " alive " + alive + "\n" + this.stats.toString();
	}
}
