package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.*;

public class EventDraw extends Event {
	public static final int ID = 5;
	Player p;
	int num;

	public EventDraw(Player p, int num) {
		super(ID);
		this.p = p;
		this.num = num;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		int i;
		for (i = 0; i < num; i++) {
			if (p.deck.cards.size() == 0) {
				// lose game
			} else {
				// this code is fucked
				Target t = new Target(this.p.deck.cards.get(0));
				if (p.hand.cards.size() < p.hand.maxsize) {
					(new EventPutCard(this.p, t, CardStatus.HAND, this.p.team, 10)).resolve(eventlist, loopprotection);
				} else {
					(new EventDestroy(t)).resolve(eventlist, loopprotection);
				}
			}
		}
	}

	public String toString() {
		return this.id + " " + p.team + " " + num + " ";
	}

	public static EventDraw fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		int num = Integer.parseInt(st.nextToken());
		return new EventDraw(p, num);
	}

	public boolean conditions() {
		return true;
	}
}
