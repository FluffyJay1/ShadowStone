package server.event;

import java.util.StringTokenizer;

import server.Board;
import server.card.Target;
import server.card.effect.Effect;

//basically for display purposes
public class EventFlag extends Event {
	public static final int ID = 21;
	public Effect effect;
	public int param;

	public EventFlag(Effect effect) {
		super(ID);
		this.effect = effect;
		this.resolvefirst = true;
	}

	public EventFlag(Effect effect, int param) {
		this(effect);
		this.param = param;
	}

	@Override
	public String toString() {
		return this.id + " " + this.effect.toReference() + this.param + " " + "\n";
	}

	public static EventFlag fromString(Board b, StringTokenizer st) {
		Effect effect = Effect.fromReference(b, st);
		int param = Integer.parseInt(st.nextToken());
		return new EventFlag(effect, param);
	}

}
