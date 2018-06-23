package server.card;

import cardpack.basic.Fighter;
import cardpack.basic.Fireball;
import cardpack.basic.Goblin;
import cardpack.basic.Tiny;

public class CardIDLinker {
	public static Class<? extends Card> getClass(int id) {
		switch (id) {
		case Goblin.ID:
			return Goblin.class;
		case Fighter.ID:
			return Fighter.class;
		case Fireball.ID:
			return Fireball.class;
		case Tiny.ID:
			return Tiny.class;
		default:
			return null;
		}
	}
}
