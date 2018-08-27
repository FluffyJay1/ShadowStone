package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.BoardObject;
import server.card.Card;
import server.card.Target;

public class EventBanish extends Event {
	public static final int ID = 18;
	Target t;

	public EventBanish(Target t) {
		super(ID);
		this.t = t;
	}

	public EventBanish(Card c) {
		super(ID);
		this.t = new Target(c);
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		for (Card c : this.t.getTargets()) {
			if (c.alive) {
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
					}
					break;
				case DECK:
					c.board.getPlayer(c.team).deck.cards.remove(c);
					c.board.getPlayer(c.team).deck.updatePositions();
					break;
				default:
					break;
				}
				c.cardpos = -1; // just in case
			}
		}
	}

	@Override
	public String toString() {
		return this.id + " " + this.t.toString() + "\n";
	}

	public static EventBanish fromString(Board b, StringTokenizer st) {
		Target t = Target.fromString(b, st);
		return new EventBanish(t);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
