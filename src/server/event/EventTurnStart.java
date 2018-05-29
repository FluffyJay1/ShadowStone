package server.event;

import java.util.LinkedList;

import server.Player;
import server.card.BoardObject;
import server.card.Minion;

public class EventTurnStart extends Event {
	Player p;

	public EventTurnStart(Player p) {
		this.p = p;
	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.p.unleashedThisTurn = false;
		eventlist.add(new EventManaChange(this.p, 1, true, false));
		eventlist.add(new EventManaChange(this.p, this.p.maxmana + 1, false, true));
		Minion leader = (Minion) this.p.board.getBoardObject(this.p.team, 0);
		eventlist.addAll(leader.onTurnStart());
		for (BoardObject b : this.p.board.getBoardObjects(this.p.team)) {
			eventlist.addAll(b.onTurnStart());
			if (b instanceof Minion) {
				((Minion) b).summoningSickness = false;
				((Minion) b).attacksThisTurn = 0;
			}
		}
		eventlist.add(new EventDraw(this.p, 1));
		return this.toString();
	}

	public String toString() {
		return "turnstart " + this.p.toString();
	}

	public boolean conditions() {
		return true;
	}
}
