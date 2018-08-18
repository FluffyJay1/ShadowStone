package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.Card;
import server.card.Deck;

//TODO: SEE IF THIS EVENT IS EVEN NEEDED
public class EventMill extends Event {
	public static final int ID = 7;
	Player p;
	Card c;

	public EventMill(Player p, Card c) {
		super(ID);
		this.p = p;
		this.c = c;
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		p.deck.cards.remove(c);
		p.deck.updatePositions();
	}

	@Override
	public String toString() {
		return this.id + " " + p.team + " " + this.c.toReference();
	}

	public static EventMill fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		Card c = Card.fromReference(b, st);
		return new EventMill(p, c);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
