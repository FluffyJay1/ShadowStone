package server.card.cardpack.basic;

import server.card.*;
import server.card.cardpack.*;

public abstract class CardSetBasic extends CardSet {
	public static final CardSet SET = new CardSet(Goblin.ID, Fighter.ID, Fireball.ID, Tiny.ID, WellOfDestination.ID,
			BellringerAngel.ID, GenesisOfLegend.ID, WoodOfBrambles.ID, Fairy.ID, Skeleton.ID, Baneling.ID,
			CursedStone.ID, Knight.ID, WeatheredVanguard.ID, DragonOracle.ID, Curate.ID, Puppet.ID, PuppetRoom.ID);
	public static final CardSet PLAYABLE_SET = new CardSet(Goblin.ID, Fighter.ID, Fireball.ID, Tiny.ID,
			WellOfDestination.ID, BellringerAngel.ID, GenesisOfLegend.ID, WoodOfBrambles.ID, Baneling.ID,
			CursedStone.ID, WeatheredVanguard.ID, DragonOracle.ID, Curate.ID, PuppetRoom.ID);

	public static Class<? extends Card> getCardClass(int id) {
		// next id is 19
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
		case Baneling.ID:
			return Baneling.class;
		case CursedStone.ID:
			return CursedStone.class;
		case Knight.ID:
			return Knight.class;
		case WeatheredVanguard.ID:
			return WeatheredVanguard.class;
		case DragonOracle.ID:
			return DragonOracle.class;
		case Curate.ID:
			return Curate.class;
		case Puppet.ID:
			return Puppet.class;
		case PuppetRoom.ID:
			return PuppetRoom.class;
		default:
			return null;
		}

	}
}
