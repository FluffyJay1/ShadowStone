package server.playeraction;

import java.util.*;

import server.*;
import server.event.*;

public class EndTurnAction extends PlayerAction {

	public static final int ID = 4;

	public int team;

	public EndTurnAction(int team) {
		super(ID);

		this.team = team;
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Event> perform(Board b) {
		if (team == b.currentPlayerTurn) {
			return b.endCurrentPlayerTurn();
		}
		return new LinkedList<Event>();
	}

	@Override
	public String toString() {
		return this.id + " " + this.team + "\n";
	}

	public static EndTurnAction fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		return new EndTurnAction(team);
	}
}
