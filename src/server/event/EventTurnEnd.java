package server.event;

import java.util.LinkedList;

import server.Player;
import server.card.BoardObject;
import server.card.Minion;

public class EventTurnEnd extends Event {
	Player p;

	public EventTurnEnd(Player p) {
		this.p = p;
	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		if (!conditions()) {
			return this.toString();
		}
		String eventstring = this.toString();
		Minion leader = (Minion) this.p.board.getBoardObject(this.p.team, 0);
		// eventlist.addAll(leader.onTurnEnd());
		eventstring += Event.resolveAll(leader.onTurnEnd(), loopprotection);

		for (BoardObject b : this.p.board.getBoardObjects(this.p.team)) {
			// eventlist.addAll(b.onTurnEnd());
			eventstring += Event.resolveAll(b.onTurnEnd(), loopprotection);
		}
		return eventstring;
	}

	public String toString() {
		return "turnend " + this.p.toString();
	}

	public boolean conditions() {
		return true;
	}
}
