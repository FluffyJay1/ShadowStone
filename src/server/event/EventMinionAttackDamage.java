package server.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.Card;
import server.card.Leader;
import server.card.Minion;
import server.card.Target;
import server.card.effect.EffectStats;

public class EventMinionAttackDamage extends Event {
	// damage phase of attack
	public static final int ID = 9;
	public Minion m1, m2;

	public EventMinionAttackDamage(Minion m1, Minion m2) {
		super(ID);
		this.m1 = m1;
		this.m2 = m2;
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		ArrayList<Minion> minions = new ArrayList<Minion>();
		minions.add(this.m1);
		minions.add(this.m2);
		ArrayList<Integer> damage = new ArrayList<Integer>(2);
		damage.add(m2.finalStatEffects.getStat(EffectStats.ATTACK));
		damage.add(m1.finalStatEffects.getStat(EffectStats.ATTACK));
		ArrayList<Boolean> poison = new ArrayList<Boolean>(2);
		poison.add(m2.finalStatEffects.getStat(EffectStats.POISONOUS) > 0);
		poison.add(m1.finalStatEffects.getStat(EffectStats.POISONOUS) > 0);
		eventlist.add(new EventDamage(minions, damage, poison));
		ArrayList<Card> baned = new ArrayList<Card>(2);
		if (m1.finalStatEffects.getStat(EffectStats.BANE) > 0 && !(m2 instanceof Leader)) {
			baned.add(m2);
		}
		if (m2.finalStatEffects.getStat(EffectStats.BANE) > 0 && !(m1 instanceof Leader)) {
			baned.add(m1);
		}
		if (!baned.isEmpty()) {
			Target t = new Target(null, 2, "");
			t.setTargets(baned);
			eventlist.add(new EventDestroy(t));
		}
	}

	@Override
	public String toString() {
		return this.id + " " + m1.toReference() + m2.toReference() + "\n";
	}

	public static EventMinionAttackDamage fromString(Board b, StringTokenizer st) {
		Card m1 = Card.fromReference(b, st);
		Card m2 = Card.fromReference(b, st);
		return new EventMinionAttackDamage((Minion) m1, (Minion) m2);
	}

	@Override
	public boolean conditions() {
		return m1.isInPlay() && m2.isInPlay();
	}
}
