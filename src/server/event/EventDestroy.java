package server.event;

import java.util.LinkedList;

import server.card.BoardObject;

public class EventDestroy extends Event {
	// killing things
	BoardObject b;

	public EventDestroy(BoardObject b) {
		this.b = b;
	}

	@Override
	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		b.board.removeBoardObject(b.position);
		b.alive = false;
		if (!loopprotection) {
			eventlist.addAll(this.b.lastWords());
		}
		return this.toString();
	}

	@Override
	public String toString() {
		return "dstry " + b.position + "\n";
	}
}
