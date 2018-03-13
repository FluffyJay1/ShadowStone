package server.card;

import server.Board;

public class Leader extends Minion {
	public Leader(Board b, String name, String text) {
		super(b, 0, 0, 0, 25, name, text, "res/leader/smile.png", 0);
	}

	public String toString() {
		return "Leader " + name + " alive " + alive + "\n" + Minion.statsToString(attack, magic, health);
	}
}
