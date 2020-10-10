package server.event;

import java.util.*;

import server.*;
import server.card.*;

public class EventTurnEnd extends Event {
	public static final int ID = 14;
	public Player p;

	public EventTurnEnd(Player p) {
		super(ID, false);
		this.p = p;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		for (BoardObject b : this.p.board.getBoardObjects(this.p.team, true, true, true)) {
			eventlist.addAll(b.onTurnEnd());
		}
		for (BoardObject b : this.p.board.getBoardObjects(this.p.team * -1, true, true, true)) {
			eventlist.addAll(b.onTurnEndEnemy());
		}
	}

	@Override
	public void undo() {
		// nothing lmao
	}

	@Override
	public String toString() {
		return this.id + " " + this.p.team + "\n";
	}

	public static EventTurnEnd fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		return new EventTurnEnd(p);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
