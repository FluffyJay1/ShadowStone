package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.BoardObject;
import server.card.Card;
import server.card.Minion;

public class EventTurnEnd extends Event {
	public static final int ID = 14;
	Player p;

	public EventTurnEnd(Player p) {
		super(ID);
		this.p = p;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		Minion leader = (Minion) this.p.board.getBoardObject(this.p.team, 0);
		eventlist.addAll(leader.onTurnEnd());
		for (BoardObject b : this.p.board.getBoardObjects(this.p.team)) {
			eventlist.addAll(b.onTurnEnd());
		}
	}

	public String toString() {
		return this.id + " " + this.p.team + "\n";
	}

	public static EventTurnEnd fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		return new EventTurnEnd(p);
	}

	public boolean conditions() {
		return true;
	}
}
