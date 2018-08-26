package server.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.Card;
import server.card.CardStatus;
import server.card.Minion;
import server.card.Target;
import server.card.effect.EffectStats;

public class EventDamage extends Event {
	// whenever damage is dealt
	public static final int ID = 3;
	public ArrayList<Integer> damage;
	public ArrayList<Target> t;

	public EventDamage(ArrayList<Target> t, ArrayList<Integer> damage) {
		super(ID);
		this.t = new ArrayList<Target>();
		this.t.addAll(t);
		this.damage = new ArrayList<Integer>();
		this.damage.addAll(damage);
	}

	public EventDamage(Target t, int damage) {
		super(ID);
		this.t = new ArrayList<Target>();
		this.t.add(t);
		this.damage = new ArrayList<Integer>();
		this.damage.add(damage);
	}

	public EventDamage(Minion m, int damage) {
		super(ID);
		this.t = new ArrayList<Target>();
		Target t = new Target(m);
		this.t.add(t);
		this.damage = new ArrayList<Integer>();
		this.damage.add(damage);
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		for (int i = 0; i < this.t.size(); i++) { // whatever
			for (Card c : this.t.get(i).getTargets()) { // sure
				if (c instanceof Minion) {
					Minion m = (Minion) c;
					if (m.health > 0 && m.health <= damage.get(i)) {
						eventlist.add(new EventDestroy(m));
					}
					m.health -= damage.get(i);
					if (!loopprotection) {
						eventlist.addAll(m.onDamaged(damage.get(i)));
					}

				}
			}
		}
	}

	@Override
	public String toString() {
		String ret = this.id + " " + this.t.size() + " ";
		for (int i = 0; i < this.t.size(); i++) {
			ret += this.t.get(i).toString() + this.damage.get(i) + " ";
		}
		return ret + "\n";
	}

	public static EventDamage fromString(Board b, StringTokenizer st) {
		int size = Integer.parseInt(st.nextToken());
		ArrayList<Target> t = new ArrayList<Target>(size);
		ArrayList<Integer> damage = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			Target ta = Target.fromString(b, st);
			int d = Integer.parseInt(st.nextToken());
			t.add(ta);
			damage.add(d);
		}
		return new EventDamage(t, damage);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
