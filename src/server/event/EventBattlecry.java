package server.event;

import java.util.StringTokenizer;

import server.Board;
import server.card.Target;
import server.card.effect.Effect;

//basically for display purposes
public class EventBattlecry extends Event {
	public static final int ID = 19;
	public Effect effect;

	public EventBattlecry(Effect effect) {
		super(ID);
		this.effect = effect;
		this.resolvefirst = true;
	}

	@Override
	public String toString() {
		return this.id + " " + this.effect.toReference() + "\n";
	}

	public static EventBattlecry fromString(Board b, StringTokenizer st) {
		Effect effect = Effect.fromReference(b, st);
		return new EventBattlecry(effect);
	}

}
