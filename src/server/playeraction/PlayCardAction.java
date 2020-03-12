package server.playeraction;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

public class PlayCardAction extends PlayerAction {

	public static final int ID = 1;
	public Player p;
	public Card c;
	public int pos;
	String battlecryTargets;

	public PlayCardAction(Player p, Card c, int pos, String battlecryTargets) {
		super(ID);

		this.p = p;
		this.c = c;
		this.pos = pos;
		this.battlecryTargets = battlecryTargets;
	}

	// remember to set battlecry targets
	@Override
	public List<Event> perform(Board b) {
		if (!this.p.canPlayCard(this.c)) { // just to be safe
			return new LinkedList<Event>();
		}
		Target.setListFromString(c.getBattlecryTargets(), b, new StringTokenizer(this.battlecryTargets));
		b.eventlist.add(new EventPlayCard(p, c, pos));
		return b.resolveAll();
	}

	@Override
	public String toString() {
		return this.ID + " " + this.p.team + " " + this.c.toReference() + this.pos + " " + this.battlecryTargets + "\n"; // YEAHH
	}

	public static PlayCardAction fromString(Board b, StringTokenizer st) {
		Player p = b.getPlayer(Integer.parseInt(st.nextToken()));
		Card c = Card.fromReference(b, st);
		int pos = Integer.parseInt(st.nextToken());
		Target.setListFromString(c.getBattlecryTargets(), b, st);
		return new PlayCardAction(p, c, pos, Target.listToString(c.getBattlecryTargets()));
	}

}
