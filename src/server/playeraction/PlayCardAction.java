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

	public PlayCardAction(Player p, Card c, int pos) {
		super(ID);

		this.p = p;
		this.c = c;
		this.pos = pos;
		// TODO Auto-generated constructor stub
	}

	// remember to set battlecry targets
	@Override
	public boolean perform(Board b) {
		if (!p.canPlayCard(c)) { // just to be safe
			return false;
		}

		b.eventlist.add(new EventPlayCard(p, c, pos));
		b.resolveAll();
		return true;
	}

	@Override
	public String toString() {
		return this.ID + " " + this.p.team + " " + this.c.toReference() + " " + this.pos + " "
				+ c.battlecryTargetsToString() + "\n"; // YEAHH
	}

	public static PlayCardAction fromString(Board b, StringTokenizer st) {
		Player p = b.getPlayer(Integer.parseInt(st.nextToken()));
		Card c = Card.fromReference(b, st);
		c.battlecryTargetsFromString(b, st);
		int pos = Integer.parseInt(st.nextToken());
		return new PlayCardAction(p, c, pos);
	}

}
