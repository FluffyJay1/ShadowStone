package server.card.cardpack.basic;

import server.card.cardpack.*;

public abstract class CardSetBasic {
    public static final CardSet SET = new CardSet(Goblin.class, Fighter.class, Fireball.class, Tiny.class,
            WellOfDestination.class, BellringerAngel.class, GenesisOfLegend.class, WoodOfBrambles.class, Fairy.class,
            Skeleton.class, Baneling.class, CursedStone.class, Knight.class, WeatheredVanguard.class,
            DragonOracle.class, Curate.class, Puppet.class, PuppetRoom.class, Beastmaster.class, StonetuskBoar.class);
    public static final CardSet PLAYABLE_SET = new CardSet(Goblin.class, Fighter.class, Fireball.class, Tiny.class,
            WellOfDestination.class, BellringerAngel.class, GenesisOfLegend.class, WoodOfBrambles.class, Baneling.class,
            CursedStone.class, WeatheredVanguard.class, DragonOracle.class, Curate.class, PuppetRoom.class, Beastmaster.class, StonetuskBoar.class);

}
