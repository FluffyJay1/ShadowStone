package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.Minion;
import server.card.effect.EffectStats;

public class EventPlayCard extends Event {
	public static final int ID = 11;
	public Player p;
	public Card c;
	int position;

	public EventPlayCard(Player p, Card c, int position) {
		super(ID);
		this.p = p;
		this.c = c;
		this.position = position;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		// p.hand.cards.remove(c);
		p.mana -= c.finalStatEffects.getStat(EffectStats.COST);
		if (c instanceof BoardObject) {
			eventlist.add(new EventPutCard(this.p, this.c, CardStatus.BOARD, this.p.team, this.position));
		} else {
			eventlist.add(new EventDestroy(this.c));
		}
		// p.hand.updatePositions();

		eventlist.addAll(c.battlecry());
	}

	public String toString() {
		return this.id + " " + p.team + " " + position + " " + this.c.toReference() + c.battlecryTargetsToString()
				+ "\n";
	}

	public static EventPlayCard fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		int position = Integer.parseInt(st.nextToken());
		Card c = Card.fromReference(b, st);
		c.battlecryTargetsFromString(b, st);
		return new EventPlayCard(p, c, position);
	}

	public boolean conditions() {
		return p.mana >= c.finalStatEffects.getStat(EffectStats.COST) && this.c.status.equals(CardStatus.HAND);
	}
}
