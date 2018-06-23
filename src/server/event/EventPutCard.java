package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.Card;
import server.card.CardStatus;

public class EventPutCard extends Event {
	// for effects that put specific cards in hand or just draw cards
	public static final int ID = 12;
	Player p;
	Card c;
	String prevreference;

	public EventPutCard(Player p, Card c) {
		super(ID);
		this.p = p;
		this.c = c;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		c.status = CardStatus.HAND;
		c.cardpos = p.hand.cards.size();
		p.hand.cards.add(c);
		p.deck.cards.remove(c);
		p.hand.updatePositions();
		p.deck.updatePositions();
	}

	public String toString() {
		return this.id + " " + p.team + " " + this.c.toReference();
	}

	public static EventPutCard fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		Card c = Card.fromReference(b, st);
		return new EventPutCard(p, c);
	}

	public boolean conditions() {
		return p.hand.cards.size() < p.hand.maxsize;
	}
}
