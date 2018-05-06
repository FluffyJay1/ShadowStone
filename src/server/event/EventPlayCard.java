package server.event;

import java.util.LinkedList;

import server.Player;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.Minion;

public class EventPlayCard extends Event {
	Player p;
	Card c;
	int position;

	public EventPlayCard(Player p, Card c, int position) {
		this.p = p;
		this.c = c;
		this.position = position;
	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		// TODO implement
		if (c instanceof BoardObject) {
			p.hand.cards.remove(c);
			p.board.addBoardObject((BoardObject) c, position);
			// c.status = CardStatus.BOARD; //happens in addboardobject
		}
		eventlist.addAll(c.battlecry());
		return this.toString();
	}

	public String toString() {
		return "playc " + p.team + c.toString();
	}

	public boolean conditions() {
		return true;
	}
}
