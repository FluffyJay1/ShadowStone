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
	int position, handpos;

	public EventPlayCard(Player p, Card c, int position) {
		this.p = p;
		this.c = c;
		this.position = position;
	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.handpos = c.handpos;
		p.hand.cards.remove(c);
		if (c instanceof BoardObject) {
			p.board.addBoardObject((BoardObject) c, position);
			c.scale = 1;
			// c.status = CardStatus.BOARD; //happens in addboardobject
		} else {
			// shadows increase by one
		}
		for (int i = c.handpos; i < p.hand.cards.size(); i++) {
			p.hand.cards.get(i).handpos--;
		}
		eventlist.addAll(c.battlecry());
		return this.toString();
	}

	public String toString() {
		return "playc " + p.team + " " + handpos + " " + c.toString() + " " + c.battlecryTargetsToString();
	}

	public boolean conditions() {
		return true;
	}
}
