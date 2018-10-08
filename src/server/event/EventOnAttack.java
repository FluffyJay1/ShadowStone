package server.event;

import java.util.StringTokenizer;

import server.Board;
import server.card.Card;
import server.card.Minion;
import server.card.Target;
import server.card.effect.Effect;

//basically for display purposes
public class EventOnAttack extends Event {
	public static final int ID = 25;
	public Effect effect;
	public Minion m;

	public EventOnAttack(Effect effect) {
		super(ID);
		this.effect = effect;
		this.priority = 1;
	}

	public EventOnAttack(Effect effect, Minion m) {
		this(effect);
		this.m = m;
	}

	@Override
	public String toString() {
		return this.id + " " + this.effect.toReference() + (this.m != null ? this.m.toReference() : "null ") + "\n";
	}

	public static EventOnAttack fromString(Board b, StringTokenizer st) {
		Effect effect = Effect.fromReference(b, st);
		Minion m = (Minion) Card.fromReference(b, st);
		return new EventOnAttack(effect, m);
	}

	@Override
	public boolean conditions() {
		return ((Minion) this.effect.owner).isInPlay();
	}

}
