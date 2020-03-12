package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventPlayCard extends Event {
	public static final int ID = 11;
	public Player p;
	public Card c;
	int position;

	public EventPlayCard(Player p, Card c, int position) {
		super(ID, false);
		this.p = p;
		this.c = c;
		this.position = position;
		this.priority = 1;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		// p.hand.cards.remove(c);
		eventlist.add(new EventManaChange(this.p, -c.finalStatEffects.getStat(EffectStats.COST), false, true));
		if (c instanceof BoardObject) {
			eventlist.add(new EventPutCard(this.p, this.c, CardStatus.BOARD, this.p.team, this.position));
		} else {
			eventlist.add(new EventDestroy(this.c));
		}
		// p.hand.updatePositions();
		eventlist.addAll(c.battlecry());
	}

	@Override
	public void undo() {
		// hmm
	}

	@Override
	public String toString() {
		return this.id + " " + p.team + " " + position + " " + this.c.toReference()
				+ Target.listToString(this.c.getBattlecryTargets()) + "\n";
	}

	public static EventPlayCard fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		int position = Integer.parseInt(st.nextToken());
		Card c = Card.fromReference(b, st);
		Target.setListFromString(c.getBattlecryTargets(), b, st);
		return new EventPlayCard(p, c, position);
	}

	@Override
	public boolean conditions() {
		return p.mana >= c.finalStatEffects.getStat(EffectStats.COST) && this.c.status.equals(CardStatus.HAND);
	}
}
