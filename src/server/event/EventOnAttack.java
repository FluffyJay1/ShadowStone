package server.event;

import java.util.StringTokenizer;

import server.Board;
import server.card.Minion;
import server.card.Target;
import server.card.effect.Effect;

//basically for display purposes
public class EventOnAttack extends Event {
	public static final int ID = 25;
	public Effect effect;
	public Target t = new Target(null);

	public EventOnAttack(Effect effect) {
		super(ID);
		this.effect = effect;
		this.priority = 1;
	}

	public EventOnAttack(Effect effect, Target t) {
		this(effect);
		this.t = t;
	}

	@Override
	public String toString() {
		return this.id + " " + this.effect.toReference() + this.t.toString() + "\n";
	}

	public static EventOnAttack fromString(Board b, StringTokenizer st) {
		Effect effect = Effect.fromReference(b, st);
		Target t = Target.fromString(b, st);
		return new EventOnAttack(effect, t);
	}

	@Override
	public boolean conditions() {
		return ((Minion) this.effect.owner).isInPlay();
	}

}
