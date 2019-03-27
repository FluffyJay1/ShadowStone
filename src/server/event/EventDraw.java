package server.event;

import java.util.*;

import server.*;
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

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		int i;
		for (i = 0; i < num; i++) {
			if (p.deck.cards.size() == 0) {
				// lose the game
				eventlist.add(new EventGameEnd(p.board, p.team * -1));
			} else {
				// TODO MAKE THIS NOT GARBAGE
				if (p.hand.cards.size() < p.hand.maxsize) {
					(new EventPutCard(this.p, this.p.deck.cards.get(0), CardStatus.HAND, this.p.team,
							this.p.hand.maxsize)).resolve(eventlist, loopprotection);
				} else {
					(new EventMill(this.p, this.p.deck.cards.get(0))).resolve(eventlist, loopprotection);
				}
			}
		}
	}

	@Override
	public String toString() {
		return this.id + " " + p.team + " " + num + "\n";
	}

	public static EventDraw fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		int num = Integer.parseInt(st.nextToken());
		return new EventDraw(p, num);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
