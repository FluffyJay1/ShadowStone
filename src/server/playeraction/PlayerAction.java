package server.playeraction;

import java.util.StringTokenizer;

import server.Board;

public abstract class PlayerAction {
	int id = 0; // literally just copying off of event

	public PlayerAction(int id) {
		this.id = id;
	}

	public void performAction(Board b) {

	}

	public String toString() {
		return this.id + "\n";
	}

	public PlayerAction createFromString(Board b, StringTokenizer st) {
		return null;
	}
}
