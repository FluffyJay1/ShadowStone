package server.event;

import java.util.*;

import server.*;
import server.card.*;

public class EventDestroy extends Event {
	// killing things
	public static final int ID = 4;
	public List<Card> c;

	public EventDestroy(Target t) {
		super(ID);
		this.c = new ArrayList<Card>();
		this.c.addAll(t.getTargets());
	}

	public EventDestroy(List<Card> c) {
		super(ID);
		this.c = new ArrayList<Card>();
		this.c.addAll(c);
	}

	public EventDestroy(Card c) {
		super(ID);
		this.c = new ArrayList<Card>();
		this.c.add(c);
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		for (Card c : this.c) {
			if (c.alive) {
				// TODO increase shadows by 1
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
							eventlist.add(new EventLeavePlay(c));
							eventlist.addAll(b.lastWords());
						}
					}
					break;
				case DECK:
					c.board.getPlayer(c.team).deck.cards.remove(c);
					c.board.getPlayer(c.team).deck.updatePositions();
					break;
				case LEADER:
					// TODO lose the game
					break;
				default:
					break;
				}
				c.cardpos = c.board.getGraveyard(c.team).size(); // just in case
				c.status = CardStatus.GRAVEYARD;
				c.board.getGraveyard(c.team).add(c);

			}
		}
	}

	@Override
	public String toString() {
		String ret = this.id + " " + this.c.size() + " ";
		for (int i = 0; i < this.c.size(); i++) {
			ret += this.c.get(i).toReference();
		}
		return ret + "\n";
	}

	public static EventDestroy fromString(Board b, StringTokenizer st) {
		int size = Integer.parseInt(st.nextToken());
		ArrayList<Card> c = new ArrayList<Card>(size);
		for (int i = 0; i < size; i++) {
			Card card = Card.fromReference(b, st);
			c.add(card);
		}
		return new EventDestroy(c);
	}

	@Override
	public boolean conditions() {
		return !this.c.isEmpty();
	}
}
