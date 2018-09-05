package server.event;

import java.util.StringTokenizer;

import server.Board;
import server.card.Minion;
import server.card.Target;
import server.card.effect.Effect;

//basically for display purposes
public class EventOnAttacked extends Event {
	public static final int ID = 26;
	public Effect effect;
	public Target t = new Target(null);

	public EventOnAttacked(Effect effect) {
		super(ID);
		this.effect = effect;
		this.resolvefirst = true;
	}

	public EventOnAttacked(Effect effect, Target t) {
		this(effect);
		this.t = t;
	}

	@Override
	public String toString() {
		return this.id + " " + this.effect.toReference() + this.t.toString() + "\n";
	}

	public static EventOnAttacked fromString(Board b, StringTokenizer st) {
		Effect effect = Effect.fromReference(b, st);
		Target t = Target.fromString(b, st);
		return new EventOnAttacked(effect, t);
	}

	@Override
	public boolean conditions() {
		return ((Minion) this.effect.owner).isInPlay();
	}

}
