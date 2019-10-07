package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

//basically for display purposes
public class EventOnAttacked extends Event {
	public static final int ID = 26;
	public Effect effect;
	public Minion m;

	public EventOnAttacked(Effect effect, boolean rng) {
		super(ID, rng);
		this.effect = effect;
		this.priority = 1;
	}

	public EventOnAttacked(Effect effect, Minion m, boolean rng) {
		this(effect, rng);
		this.m = m;
	}

	@Override
	public String toString() {
		return this.id + " " + this.rng + " " + this.effect.toReference()
				+ (this.m != null ? this.m.toReference() : "null ") + "\n";
	}

	public static EventOnAttacked fromString(Board b, StringTokenizer st) {
		boolean rng = Boolean.parseBoolean(st.nextToken());
		Effect effect = Effect.fromReference(b, st);
		Minion m = (Minion) Card.fromReference(b, st);
		return new EventOnAttacked(effect, m, rng);
	}

	@Override
	public boolean conditions() {
		return ((Minion) this.effect.owner).isInPlay();
	}

}
