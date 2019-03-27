package server.event;

import java.util.*;

import server.*;
import server.card.*;

public class EventDamage extends Event {
	// whenever damage is dealt
	public static final int ID = 3;
	public List<Integer> damage;
	public List<Minion> m;
	public List<Boolean> poisonous;

	public EventDamage(List<Minion> m, List<Integer> damage, List<Boolean> poisonous) {
		super(ID);
		this.m = new ArrayList<Minion>();
		this.damage = new ArrayList<Integer>();
		this.poisonous = new ArrayList<Boolean>();
		this.m.addAll(m);
		this.damage.addAll(damage);
		this.poisonous.addAll(poisonous);
	}

	public EventDamage(Target t, int damage, boolean poisonous) {
		super(ID);
		this.m = new ArrayList<Minion>();
		this.damage = new ArrayList<Integer>();
		for (Card card : t.getTargets()) {
			if (card instanceof Minion) {
				Minion minion = (Minion) card;
				this.m.add(minion);
				this.damage.add(damage);
			}
		}
		this.poisonous = new ArrayList<Boolean>();
		this.poisonous.add(poisonous);
	}

	public EventDamage(Minion m, int damage, boolean poisonous) {
		super(ID);
		this.m = new ArrayList<Minion>();
		this.m.add(m);
		this.damage = new ArrayList<Integer>();
		this.damage.add(damage);
		this.poisonous = new ArrayList<Boolean>();
		this.poisonous.add(poisonous);
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		for (int i = 0; i < this.m.size(); i++) { // sure
			Minion minion = m.get(i);
			if (!loopprotection) {
				eventlist.addAll(minion.onDamaged(damage.get(i)));
			}
			if ((this.poisonous.get(i) && this.damage.get(i) > 0 && !(minion instanceof Leader))
					|| (minion.health > 0 && minion.health <= damage.get(i))) {
				eventlist.add(new EventDestroy(minion));
			}
			minion.health -= damage.get(i);
		}
	}

	@Override
	public String toString() {
		String ret = this.id + " " + this.m.size() + " ";
		for (int i = 0; i < this.m.size(); i++) {
			ret += this.m.get(i).toReference() + this.damage.get(i) + " " + this.poisonous.get(i) + " ";
		}
		return ret + "\n";
	}

	public static EventDamage fromString(Board b, StringTokenizer st) {
		int size = Integer.parseInt(st.nextToken());
		ArrayList<Minion> m = new ArrayList<Minion>(size);
		ArrayList<Integer> damage = new ArrayList<Integer>(size);
		ArrayList<Boolean> poisonous = new ArrayList<Boolean>();
		for (int i = 0; i < size; i++) {
			Minion minion = (Minion) Card.fromReference(b, st);
			int d = Integer.parseInt(st.nextToken());
			boolean po = Boolean.parseBoolean(st.nextToken());
			m.add(minion);
			damage.add(d);
			poisonous.add(po);
		}
		return new EventDamage(m, damage, poisonous);
	}

	@Override
	public boolean conditions() {
		return !this.m.isEmpty();
	}
}
