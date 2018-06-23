package server.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.Card;
import server.card.CardStatus;
import server.card.Minion;
import server.card.effect.EffectStats;

public class EventDamage extends Event {
	// whenever damage is dealt
	public static final int ID = 3;
	public ArrayList<Integer> damage;
	public ArrayList<Minion> m;

	public EventDamage(ArrayList<Minion> m, ArrayList<Integer> damage) {
		super(ID);
		this.m = new ArrayList<Minion>();
		this.m.addAll(m);
		this.damage = new ArrayList<Integer>();
		this.damage.addAll(damage);
	}

	public EventDamage(Minion m, int damage) {
		super(ID);
		this.m = new ArrayList<Minion>();
		this.m.add(m);
		this.damage = new ArrayList<Integer>();
		this.damage.add(damage);
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		for (int i = 0; i < this.m.size(); i++) { // whatever
			this.m.get(i).health -= damage.get(i);
			if (!loopprotection) {
				eventlist.addAll(m.get(i).onDamaged(damage.get(i)));
			}
			if (m.get(i).health <= 0) {
				eventlist.add(new EventDestroy(m.get(i)));
			}
		}
	}

	@Override
	public String toString() {
		String ret = this.id + " " + this.m.size();
		for (int i = 0; i < this.m.size(); i++) {
			ret += this.m.get(i).toReference() + this.damage.get(i) + " ";
		}
		return ret;
	}

	public static EventDamage fromString(Board b, StringTokenizer st) {
		int size = Integer.parseInt(st.nextToken());
		ArrayList<Minion> m = new ArrayList<Minion>(size);
		ArrayList<Integer> damage = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			Card c = Card.fromReference(b, st);
			int d = Integer.parseInt(st.nextToken());
			m.add((Minion) c);
			damage.add(d);
		}
		return new EventDamage(m, damage);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
