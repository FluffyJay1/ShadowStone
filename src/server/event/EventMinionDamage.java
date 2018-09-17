package server.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.Card;
import server.card.Minion;
import server.card.Target;
import server.card.effect.EffectStats;

public class EventMinionDamage extends Event {
	// whenever a minion does damage from card text i.e. ragnaros end of turn
	// effect
	public static final int ID = 10;
	public ArrayList<Integer> damage;
	public Minion m1;
	public ArrayList<Target> m2;

	public EventMinionDamage(Minion m1, ArrayList<Target> m2, ArrayList<Integer> damage) {
		super(ID);
		this.m1 = m1;
		this.m2 = new ArrayList<Target>();
		this.m2.addAll(m2);
		this.damage = new ArrayList<Integer>();
		this.damage.addAll(damage);
		this.priority = 1;
	}

	public EventMinionDamage(Minion m1, Target m2, int damage) {
		super(ID);
		this.m1 = m1;
		this.m2 = new ArrayList<Target>();
		this.m2.add(m2);
		this.damage = new ArrayList<Integer>();
		this.damage.add(damage);
		this.priority = 1;
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
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
			ret += this.m2.get(i).toString() + this.damage.get(i) + " ";
		}
		return ret + "\n";
	}

	public static EventMinionDamage fromString(Board b, StringTokenizer st) {
		Card m1 = Card.fromReference(b, st);
		int size = Integer.parseInt(st.nextToken());
		ArrayList<Target> m2 = new ArrayList<Target>(size);
		ArrayList<Integer> damage = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			Target t = Target.fromString(b, st);
			int d = Integer.parseInt(st.nextToken());
			m2.add(t);
			damage.add(d);
		}
		return new EventMinionDamage((Minion) m1, m2, damage);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
