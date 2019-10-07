package server.event;

import java.util.*;

import server.*;
import server.card.*;

public class EventDraw extends Event {
	public static final int ID = 5;
	Player p;
	// int num;

	public EventDraw(Player p) {
		super(ID, false);
		this.p = p;
		this.priority = 1;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		if (p.deck.cards.size() == 0) {
			// lose the game
			eventlist.add(new EventGameEnd(p.board, p.team * -1));
		} else {
			// TODO MAKE THIS LESS GARBAGE
			if (p.hand.cards.size() < p.hand.maxsize) {
				eventlist.add(new EventPutCard(this.p, this.p.deck.cards.get(0), CardStatus.HAND, this.p.team,
						this.p.hand.maxsize));
			} else {
				eventlist.add(new EventMill(this.p, this.p.deck.cards.get(0)));
			}
		}
	}

	@Override
	public void undo() {
		// not much we can do here chief, everything is events
	}

	@Override
	public String toString() {
		return this.id + " " + p.team + "\n";
	}

	public static EventDraw fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		return new EventDraw(p);
	}

	@Override
	public boolean conditions() {
		return true;
	}

	// just a helper method to add back in the feature of drawing multiple cards
	public static void drawMultiple(List<Event> eventlist, boolean loopprotection, Player p, int num) {
		for (int i = 0; i < num; i++) {
			eventlist.add(new EventDraw(p));
		}
	}
}
