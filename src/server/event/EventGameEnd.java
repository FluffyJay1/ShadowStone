package server.event;

import java.util.*;

import server.*;

public class EventGameEnd extends Event {

	public static final int ID = 28;
	public int victory;
	Board b;

	public EventGameEnd(Board b, int victory) {
		super(ID);
		this.b = b;
		this.priority = 10000;
		this.victory = victory;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.b.winner = victory;
		// System.exit(0); // YES
	}

	@Override
	public String toString() {
		return this.id + " " + victory + "\n";
	}

	public static EventGameEnd fromString(Board b, StringTokenizer st) {
		int vict = Integer.parseInt(st.nextToken());
		return new EventGameEnd(b, vict);
	}
}
