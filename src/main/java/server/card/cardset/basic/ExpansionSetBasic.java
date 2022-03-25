package server.card.cardset.basic;

import server.card.cardset.*;
import server.card.cardset.basic.bloodwarlock.*;
import server.card.cardset.basic.dragondruid.*;
import server.card.cardset.basic.forestrogue.*;
import server.card.cardset.basic.havenpriest.*;
import server.card.cardset.basic.neutral.*;
import server.card.cardset.basic.portalhunter.*;
import server.card.cardset.basic.runemage.*;
import server.card.cardset.basic.shadowshaman.*;
import server.card.cardset.basic.swordpaladin.*;

public class ExpansionSetBasic extends ExpansionSet {
    public static final CardSet SET = new CardSet(new Goblin(), new Fighter(), new Fireball(), new Tiny(),
            new WellOfDestination(), new BellringerAngel(), new GenesisOfLegend(), new WoodOfBrambles(), new Fairy(),
            new Skeleton(), new Baneling(), new CursedStone(), new Knight(), new WeatheredVanguard(),
            new DragonOracle(), new Curate(), new Puppet(), new PuppetRoom(), new Beastmaster(), new StonetuskBoar(),
            new BlackenedScripture(), new MordecaiTheDuelist(), new HallowedDogma(), new HolyFalcon(), new HolyflameTiger(),
            new BeastcallAria(), new SiegeTank(), new Batter(), new Spectre(), new Chronos(), new Rhinoceroach(),
            new Zombie(), new DemonlordEachtar(), new MoltenGiant(), new FatesHand(), new Snowman(), new SummonSnow());
    public static final CardSet PLAYABLE_SET = new CardSet(new Goblin(), new Fighter(), new Fireball(), new Tiny(),
            new WellOfDestination(), new BellringerAngel(), new GenesisOfLegend(), new WoodOfBrambles(), new Baneling(),
            new CursedStone(), new WeatheredVanguard(), new DragonOracle(), new Curate(), new PuppetRoom(), new Beastmaster(),
            new StonetuskBoar(), new BlackenedScripture(), new MordecaiTheDuelist(), new HallowedDogma(), new BeastcallAria(),
            new SiegeTank(), new Batter(), new Chronos(), new Rhinoceroach(), new DemonlordEachtar(), new MoltenGiant(),
            new FatesHand(), new SummonSnow());

    @Override
    public CardSet getCards() {
        return new CardSet(PLAYABLE_SET);
    }
}
