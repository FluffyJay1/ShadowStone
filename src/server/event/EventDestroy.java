package server.event;

import java.util.LinkedList;

import server.card.*;

public class EventDestroy extends Event {
	// killing things
	Card c;

	public EventDestroy(Card c) {
		this.c = c;
	}

	@Override
	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.c.alive = false;
		if (this.c instanceof BoardObject && this.c.status == CardStatus.BOARD) {
			BoardObject b = (BoardObject) this.c;
			b.board.removeBoardObject(b.team, b.boardpos);
			if (!loopprotection) {
				eventlist.addAll(b.lastWords());
			}
		}

		return this.toString();
	}

	@Override
	public String toString() {
		return "dstry " + this.c.toString() + "\n";
	}
}
