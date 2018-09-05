package server.card.effect;

import java.util.StringTokenizer;

import client.Game;
import server.Board;
import server.card.Card;

public class EffectStatChange extends Effect {
	public static final int ID = -1;

	public EffectStatChange(String description) {
		super(ID, description);
	}

	public static EffectStatChange fromString(Board b, StringTokenizer st) {
		Card owner = Card.fromReference(b, st);
		String description = st.nextToken(Game.STRING_END).trim();
		st.nextToken(" \n"); // THANKS STRING TOKENIZER
		EffectStatChange esc = new EffectStatChange(description);
		esc.owner = owner;
		esc.set = EffectStats.fromString(st);
		esc.change = EffectStats.fromString(st);
		return esc;
	}
}
