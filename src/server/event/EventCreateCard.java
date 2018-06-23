package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.Deck;
import server.card.Hand;

public class EventCreateCard extends Event {
	public static final int ID = 2;
	Card c;
	Board b;
	CardStatus status;
	int team, cardpos;

	public EventCreateCard(Board b, Card c, int team, CardStatus status, int cardpos) {
		super(ID);
		this.c = c;
		this.b = b;
		this.team = team;
		this.status = status;
		this.cardpos = cardpos;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		switch (this.status) {
		case HAND:
			Hand relevantHand = this.b.getPlayer(this.team).hand;
			if (relevantHand.cards.size() >= relevantHand.maxsize) {
				eventlist.add(new EventDestroy(c));
			} else {
				relevantHand.cards.add(this.cardpos, this.c);
				relevantHand.updatePositions();
			}
			break;
		case BOARD:
			if (this.c instanceof BoardObject) {
				this.b.addBoardObject((BoardObject) this.c, this.team, this.cardpos);
			}
			break;
		case DECK:
			Deck relevantDeck = this.b.getPlayer(this.team).deck;
			relevantDeck.cards.add(this.cardpos, this.c);
			relevantDeck.updatePositions();
			break;
		default:
			break;
		}
	}

	public String toString() {
		return this.id + " " + this.c.toConstructorString() + " " + this.team + " " + this.status.toString() + " "
				+ this.cardpos + " ";
	}

	public static EventCreateCard fromString(Board b, StringTokenizer st) {
		Card c = Card.createFromConstructorString(b, st);
		int team = Integer.parseInt(st.nextToken());
		String sStatus = st.nextToken();
		CardStatus csStatus = null;
		for (CardStatus cs : CardStatus.values()) {
			if (cs.toString().equals(sStatus)) {
				csStatus = cs;
			}
		}
		int cardpos = Integer.parseInt(st.nextToken());
		return new EventCreateCard(b, c, team, csStatus, cardpos);
	}

	public boolean conditions() {
		return true;
	}
}
