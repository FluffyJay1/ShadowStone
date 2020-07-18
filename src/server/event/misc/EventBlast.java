package server.event.misc;

import java.util.*;

import client.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class EventBlast extends Event {
	public static final int ID = 31;

	Board b;
	int enemyTeam;
	Effect effectSource;
	Card cardSource;
	int damage;

	public EventBlast(Card source, int damage) {
		super(ID, true);
		this.b = source.board;
		this.enemyTeam = source.team * -1;
		this.cardSource = source;
		this.damage = damage;
	}

	public EventBlast(Effect source, int damage) {
		super(ID, true);
		this.b = source.owner.board;
		this.enemyTeam = source.owner.team * -1;
		this.effectSource = source;
		this.damage = damage;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		List<Minion> minions = this.b.getMinions(this.enemyTeam, false, true);
		Minion target = null;
		if (minions.isEmpty()) {
			target = this.b.getPlayer(this.enemyTeam).leader;
		} else {
			target = Game.selectRandom(minions);
		}
		if (this.effectSource != null) {
			eventlist.add(new EventEffectDamage(this.effectSource, target, this.damage));
		} else if (this.cardSource != null) {
			eventlist.add(new EventCardDamage(this.cardSource, target, this.damage));
		} else {
			eventlist.add(new EventDamage(target, this.damage, false));
		}
	}

	@Override
	public void undo() {
	}

	@Override
	public String toString() {
		return ID + " " + Effect.referenceOrNull(this.effectSource) + Card.referenceOrNull(this.cardSource)
				+ this.damage + " \n";
	}

	public static EventBlast fromString(Board b, StringTokenizer st) {
		Effect effect = Effect.fromReference(b, st);
		Card c = Card.fromReference(b, st);
		int damage = Integer.parseInt(st.nextToken());
		if (effect != null) {
			return new EventBlast(effect, damage);
		}
		if (c != null) {
			return new EventBlast(c, damage);
		}
		return null;
	}

	@Override
	public boolean conditions() {
		return (this.cardSource != null && this.cardSource.alive) || this.effectSource != null;
	}
}
