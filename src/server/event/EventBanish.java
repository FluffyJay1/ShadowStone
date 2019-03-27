package server.event;

import java.util.*;

import server.*;
import server.card.*;

public class EventBanish extends Event {
	public static final int ID = 18;
	public List<Card> c;

	public EventBanish(Target t) {
		super(ID);
		this.c = new ArrayList<Card>();
		this.c.addAll(t.getTargets());
	}

	public EventBanish(List<Card> c) {
		super(ID);
		this.c = new ArrayList<Card>();
		this.c.addAll(c);
	}

	public EventBanish(Card c) {
		super(ID);
		this.c = new ArrayList<Card>();
		this.c.add(c);
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		for (Card c : this.c) {
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
						if (!loopprotection) {
							eventlist.add(new EventLeavePlay(c));
						}
					}
					break;
				case DECK:
					c.board.getPlayer(c.team).deck.cards.remove(c);
					c.board.getPlayer(c.team).deck.updatePositions();
					break;
				default: // banish leader lmao
					break;
				}
				c.cardpos = c.board.banished.size(); // just in case
				c.board.banished.add(c);
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

	public static EventBanish fromString(Board b, StringTokenizer st) {
		int size = Integer.parseInt(st.nextToken());
		ArrayList<Card> c = new ArrayList<Card>(size);
		for (int i = 0; i < size; i++) {
			Card card = Card.fromReference(b, st);
			c.add(card);
		}
		return new EventBanish(c);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
