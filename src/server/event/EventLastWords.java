package server.event;

import java.util.StringTokenizer;

import server.Board;
import server.card.Target;
import server.card.effect.Effect;

//basically for display purposes
public class EventLastWords extends Event {
	public static final int ID = 20;
	public Effect effect;

	public EventLastWords(Effect effect) {
		super(ID);
		this.effect = effect;
		this.priority = 1;
	}

	@Override
	public String toString() {
		return this.id + " " + this.effect.toReference() + "\n";
	}

	public static EventLastWords fromString(Board b, StringTokenizer st) {
		Effect effect = Effect.fromReference(b, st);
		return new EventLastWords(effect);
	}

}
