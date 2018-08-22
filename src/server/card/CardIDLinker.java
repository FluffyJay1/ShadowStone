package server.card;

import cardpack.basic.*;
import server.card.leader.Rowen;

public class CardIDLinker {
	public static Class<? extends Card> getClass(int id) {
		switch (id) {
		case Rowen.ID:
			return Rowen.class;
		case Goblin.ID:
			return Goblin.class;
		case Fighter.ID:
			return Fighter.class;
		case Fireball.ID:
			return Fireball.class;
		case Tiny.ID:
			return Tiny.class;
		case WellOfDestination.ID:
			return WellOfDestination.class;
		default:
			return null;
		}
	}
}
