package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventMinionDamage extends Event {
	// whenever a minion does damage from card text i.e. ragnaros end of turn
	// effect
	public static final int ID = 10;
	public List<Integer> damage;
	public Minion m1;
	public List<Minion> m2;

	public EventMinionDamage(Minion m1, List<Minion> m2, List<Integer> damage) {
		super(ID);
		this.m1 = m1;
		this.m2 = new ArrayList<Minion>();
		this.m2.addAll(m2);
		this.damage = new ArrayList<Integer>();
		this.damage.addAll(damage);
		this.priority = 1;
	}

	public EventMinionDamage(Minion m1, Target m2, int damage) {
		super(ID);
		this.m1 = m1;
		this.m2 = new ArrayList<Minion>();
		this.damage = new ArrayList<Integer>();
		for (Card c : m2.getTargets()) {
			if (c instanceof Minion) {
				this.m2.add((Minion) c);
				this.damage.add(damage);
			}
		}
		this.priority = 1;
	}

	public EventMinionDamage(Minion m1, Minion m2, int damage) {
		super(ID);
		this.m1 = m1;
		this.m2 = new ArrayList<Minion>();
		this.m2.add(m2);
		this.damage = new ArrayList<Integer>();
		this.damage.add(damage);
		this.priority = 1;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		ArrayList<Boolean> poisonous = new ArrayList<Boolean>(this.m2.size());
		for (int i = 0; i < this.m2.size(); i++) {
			poisonous.add(m1.finalStatEffects.getStat(EffectStats.POISONOUS) > 0);
		}
		eventlist.add(new EventDamage(this.m2, this.damage, poisonous));
	}

	@Override
	public String toString() {
		String ret = this.id + " " + this.m1.toReference() + this.m2.size() + " ";
		for (int i = 0; i < this.m2.size(); i++) {
			ret += this.m2.get(i).toReference() + this.damage.get(i) + " ";
		}
		return ret + "\n";
	}

	public static EventMinionDamage fromString(Board b, StringTokenizer st) {
		Card m1 = Card.fromReference(b, st);
		int size = Integer.parseInt(st.nextToken());
		ArrayList<Minion> m2 = new ArrayList<Minion>(size);
		ArrayList<Integer> damage = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			Minion m = (Minion) Card.fromReference(b, st);
			int d = Integer.parseInt(st.nextToken());
			m2.add(m);
			damage.add(d);
		}
		return new EventMinionDamage((Minion) m1, m2, damage);
	}

	@Override
	public boolean conditions() {
		return !this.m2.isEmpty();
	}
}
