package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.*;

public class EventDestroy extends Event {
	// killing things
	public static final int ID = 4;
	Target t;

	public EventDestroy(Target t) {
		super(ID);
		this.t = t;
	}

	public EventDestroy(Card c) {
		super(ID);
		this.t = new Target(c);
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		for (Card c : this.t.getTargets()) {
			c.alive = false;
			switch (c.status) {
			case HAND:
				c.board.getPlayer(c.team).hand.cards.remove(c);
				c.board.getPlayer(c.team).hand.updatePositions();
				break;
			case BOARD:
				if (c instanceof BoardObject) {
					BoardObject b = (BoardObject) c;
					b.board.removeBoardObject(b.team, b.cardpos);
					if (!loopprotection) {
						eventlist.addAll(b.lastWords());
					}
				}
				break;
			case DECK:
				c.board.getPlayer(c.team).deck.cards.remove(c);
				c.board.getPlayer(c.team).deck.updatePositions();
				break;
			default:
				break;
			}
		}
	}

	@Override
	public String toString() {
		return this.id + " " + this.t.toString();
	}

	public static EventDestroy fromString(Board b, StringTokenizer st) {
		Target t = Target.fromString(b, st);
		return new EventDestroy(t);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
