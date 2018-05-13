package server.event;

import java.util.LinkedList;

import server.Player;
import server.card.Card;
import server.card.CardStatus;

public class EventPutCard {
	// for effects that put specific cards in hand or just draw cards
	Player p;
	Card c;

	public EventPutCard(Player p, Card c) {
		this.p = p;
		this.c = c;
	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		if (!this.conditions()) {
			return this.toString();
		}
		c.status = CardStatus.HAND;
		c.handpos = p.hand.cards.size();
		p.hand.cards.add(c);
		p.deck.cards.remove(c);
		return this.toString();
	}

	public String toString() {
		return "putc " + p.team + " " + c.toString() + " " + conditions() + "\n";
	}

	public boolean conditions() {
		return p.hand.cards.size() < p.hand.maxsize;
	}
}
