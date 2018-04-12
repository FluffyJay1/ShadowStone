package server.event;

import java.util.LinkedList;

import server.Player;
import server.card.*;

public class EventDraw extends Event {
	Player p;
	int num;

	public EventDraw(Player p, int num) {
		this.p = p;
		this.num = num;
	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		int i;
		String eventstring = this.toString();
		for (i = 0; i < num; i++) {
			if (p.deck.cards.size() == 0) {
				// lose game
			} else {
				if (p.hand.cards.size() < p.hand.maxsize) {
					eventstring += (new EventPutCard(p, p.deck.cards.get(0))).resolve(eventlist, loopprotection);
				} else {
					eventstring += (new EventMill(p, p.deck.cards.get(0))).resolve(eventlist, loopprotection);
				}
			}
		}
		return eventstring;
	}

	public String toString() {
		return "draw " + p.team + " " + num + "\n";
	}

	public boolean conditions() {
		return true;
	}
}
