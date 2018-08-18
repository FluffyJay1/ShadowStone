package server.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.Card;
import server.card.Minion;
import server.card.Target;
import server.card.effect.EffectStats;

public class EventRestore extends Event {
	public static final int ID = 13;
	ArrayList<Integer> heal;
	ArrayList<Target> t;

	public EventRestore(ArrayList<Target> t, ArrayList<Integer> heal) {
		super(ID);
		this.t = new ArrayList<Target>();
		this.t.addAll(t);
		this.heal = new ArrayList<Integer>();
		this.heal.addAll(heal);
	}

	public EventRestore(Target t, int heal) {
		super(ID);
		this.t = new ArrayList<Target>();
		this.t.add(t);
		this.heal = new ArrayList<Integer>();
		this.heal.add(heal);
	}

	public EventRestore(Minion m, int heal) {
		super(ID);
		this.t = new ArrayList<Target>();
		Target t = new Target(m);
		this.t.add(t);
		this.heal = new ArrayList<Integer>();
		this.heal.add(heal);
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		for (int i = 0; i < this.t.size(); i++) { // whatever
			for (Card c : this.t.get(i).getTargets()) { // sure
				if (c instanceof Minion) {
					Minion m = (Minion) c;
					m.health += this.heal.get(i);
					if (m.health > m.finalStatEffects.getStat(EffectStats.HEALTH)) {
						m.health = m.finalStatEffects.getStat(EffectStats.HEALTH);
					}
				}
			}
		}

		// TODO on healed
	}

	@Override
	public String toString() {
		String ret = this.id + " " + this.t.size();
		for (int i = 0; i < this.t.size(); i++) {
			ret += this.t.toString() + this.heal.get(i) + " ";
		}
		return ret;
	}

	public static EventRestore fromString(Board b, StringTokenizer st) {
		int size = Integer.parseInt(st.nextToken());
		ArrayList<Target> t = new ArrayList<Target>(size);
		ArrayList<Integer> heal = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++) {
			Target ta = Target.fromString(b, st);
			int h = Integer.parseInt(st.nextToken());
			t.add(ta);
			heal.add(h);
		}
		return new EventRestore(t, heal);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
