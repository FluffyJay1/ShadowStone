package server.event;

import java.util.LinkedList;

import server.Player;
import server.card.Card;
import server.card.Deck;

public class EventMill extends Event {
	Player p;
	Card c;

	public EventMill(Player p, Card c) {
		this.p = p;
		this.c = c;
	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		p.deck.cards.remove(c);
		return this.toString();
	}

	public String toString() {
		return "mill " + p.team + " " + c.toString() + "\n";
	}
}
