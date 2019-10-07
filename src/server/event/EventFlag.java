package server.event;

import java.util.*;

import server.*;
import server.card.effect.*;

//basically for display purposes
public class EventFlag extends Event {
	public static final int ID = 21;
	public Effect effect;

	public EventFlag(Effect effect, boolean rng) {
		super(ID, rng);
		this.effect = effect;
		this.priority = 1;
	}

	@Override
	public String toString() {
		return this.id + " " + this.rng + " " + this.effect.toReference() + " " + "\n";
	}

	public static EventFlag fromString(Board b, StringTokenizer st) {
		boolean rng = Boolean.parseBoolean(st.nextToken());
		Effect effect = Effect.fromReference(b, st);
		return new EventFlag(effect, rng);
	}

}
