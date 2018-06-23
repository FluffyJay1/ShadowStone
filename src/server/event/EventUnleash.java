package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.Card;
import server.card.Minion;

public class EventUnleash extends Event {
	public static final int ID = 16;
	public Player p;
	public Minion m;

	public EventUnleash(Player p, Minion m) {
		super(ID);
		this.p = p;
		this.m = m;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		// TODO add the unleash effects of individual classes
		eventlist.add(new EventManaChange(this.p, -2, false, true));
		eventlist.addAll(this.m.unleash());
		this.m.resetUnleashTargets();
		this.p.unleashedThisTurn = true;
	}

	public String toString() {
		return this.id + " " + this.p.team + " " + m.toReference() + m.unleashTargetsToString();
	}

	public static EventUnleash fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		Minion m = (Minion) Card.fromReference(b, st);
		m.unleashTargetsFromString(b, st);
		return new EventUnleash(p, m);
	}

	public boolean conditions() {
		return this.p.canUnleashCard(this.m);
	}
}
