package server.card.effect;

import java.util.*;

import client.*;
import server.*;
import server.card.*;

public class EffectStatChange extends Effect {
	public static final int ID = 1;

	public EffectStatChange(String description) {
		super(ID, description);
	}

	public EffectStatChange(String description, int attack, int magic, int health) {
		this(description);
		this.change.setStat(EffectStats.ATTACK, attack);
		this.change.setStat(EffectStats.MAGIC, magic);
		this.change.setStat(EffectStats.HEALTH, health);
	}

	public static EffectStatChange fromString(Board b, StringTokenizer st) {
		Card owner = Card.fromReference(b, st);
		String description = st.nextToken(Game.STRING_END).trim();
		st.nextToken(" \n"); // THANKS STRING TOKENIZER
		boolean mute = Boolean.parseBoolean(st.nextToken());
		EffectStatChange esc = new EffectStatChange(description);
		esc.owner = owner;
		esc.mute = mute;
		esc.set = EffectStats.fromString(st);
		esc.change = EffectStats.fromString(st);
		return esc;
	}
}
