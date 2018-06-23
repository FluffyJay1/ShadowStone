package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.*;

public class EventDestroy extends Event {
	// killing things
	public static final int ID = 4;
	Card c;

	public EventDestroy(Card c) {
		super(ID);
		this.c = c;
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.c.alive = false;
		switch (this.c.status) {
		case HAND:
			this.c.board.getPlayer(this.c.team).hand.cards.remove(this.c);
			this.c.board.getPlayer(this.c.team).hand.updatePositions();
			break;
		case BOARD:
			if (this.c instanceof BoardObject) {
				BoardObject b = (BoardObject) this.c;
				b.board.removeBoardObject(b.team, b.cardpos);
				if (!loopprotection) {
					eventlist.addAll(b.lastWords());
				}
			}
			break;
		case DECK:
			this.c.board.getPlayer(this.c.team).deck.cards.remove(this.c);
			this.c.board.getPlayer(this.c.team).deck.updatePositions();
			break;
		default:
			break;
		}
	}

	@Override
	public String toString() {
		return this.id + " " + this.c.toReference();
	}

	public static EventDestroy fromString(Board b, StringTokenizer st) {
		Card c = Card.fromReference(b, st);
		return new EventDestroy((Minion) c);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
