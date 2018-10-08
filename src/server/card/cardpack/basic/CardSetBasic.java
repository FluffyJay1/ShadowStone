package server.card.cardpack.basic;

import server.card.Card;
import server.card.cardpack.CardSet;
import server.card.leader.Rowen;
import server.card.unleashpower.UnleashImbueMagic;

public abstract class CardSetBasic extends CardSet {
	public static final int MIN_ID = 1;
	public static final int MAX_ID = 10;
	public static final CardSet SET = new CardSet(Goblin.ID, Fighter.ID, Fireball.ID, Tiny.ID, WellOfDestination.ID,
			BellringerAngel.ID, GenesisOfLegend.ID, WoodOfBrambles.ID, Fairy.ID, Skeleton.ID);
	public static final CardSet PLAYABLE_SET = new CardSet(Goblin.ID, Fighter.ID, Fireball.ID, Tiny.ID,
			WellOfDestination.ID, BellringerAngel.ID, GenesisOfLegend.ID, WoodOfBrambles.ID);

	public static Class<? extends Card> getCardClass(int id) {
		// next id is 11
		switch (id) {
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
		case BellringerAngel.ID:
			return BellringerAngel.class;
		case GenesisOfLegend.ID:
			return GenesisOfLegend.class;
		case WoodOfBrambles.ID:
			return WoodOfBrambles.class;
		case Fairy.ID:
			return Fairy.class;
		case Skeleton.ID:
			return Skeleton.class;
		default:
			return null;
		}
	}
}
