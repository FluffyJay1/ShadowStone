package server.playeraction;

import java.util.StringTokenizer;

import server.Board;

public class EndTurnAction extends PlayerAction {

	public static final int ID = 4;

	public int team;

	public EndTurnAction(int team) {
		super(ID);

		this.team = team;
		// TODO Auto-generated constructor stub
	}

	public void perform(Board b) {
		if (team == b.currentplayerturn) {
			b.endCurrentPlayerTurn();
		}
	}

	public String toString() {
		return this.id + " " + this.team + "\n";
	}

	public static EndTurnAction fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		return new EndTurnAction(team);
	}
}
