package server.event;

import java.util.*;

import server.*;
import server.card.*;

//this is different from just destroying the card because it shows the card being destroyed before thanos snapping it
public class EventMill extends Event {
	public static final int ID = 7;
	Player p;
	Card c;

	public EventMill(Player p, Card c) {
		super(ID, false);
		this.p = p;
		this.c = c;
		this.priority = 1;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		// for display purposes, shouldn't cause errors
		eventlist.add(new EventDestroy(this.c));
	}

	@Override
	public void undo() {

	}

	@Override
	public String toString() {
		return this.id + " " + p.team + " " + this.c.toReference() + "\n";
	}

	public static EventMill fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		Card c = Card.fromReference(b, st);
		return new EventMill(p, c);
	}

	@Override
	public boolean conditions() {
		return this.c.status == CardStatus.DECK;
	}
}
