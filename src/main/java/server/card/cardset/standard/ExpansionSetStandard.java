package server.card.cardset.standard;

import server.card.cardset.CardSet;
import server.card.cardset.ExpansionSet;
import server.card.cardset.standard.bloodwarlock.*;
import server.card.cardset.standard.dragondruid.*;
import server.card.cardset.standard.forestrogue.*;
import server.card.cardset.standard.havenpriest.*;
import server.card.cardset.standard.neutral.*;
import server.card.cardset.standard.portalhunter.*;
import server.card.cardset.standard.runemage.*;
import server.card.cardset.standard.shadowshaman.*;
import server.card.cardset.standard.swordpaladin.*;

public class ExpansionSetStandard extends ExpansionSet {
    public static final CardSet UNPLAYABLE_SET = new CardSet(new ArtifactRhino(), new Camieux(), new LeonidasResolve(),
            new ServantOfDarkness());

    public static final CardSet PLAYABLE_SET = new CardSet(new Belphegor(), new DemonicRam(), new DemonKey(), new EndearingSuccubusLilith(),
            new MoltenGiant(), new PrisonOfPain(), new Revelation(), new TerrorDemon(), new AielaDragonKnight(), new CanyonOfTheDragons(),
            new DracomancersRites(), new Fafnir(), new GriffonKnight(), new PyroxeneDragon(), new Rahab(), new VenomousPucewyrm(),
            new AncientForestDragon(), new Cassiopeia(), new DungeoncrawlFairy(), new Rhinoceroach(), new SukunaBraveAndSmall(), new Vanish(),
            new ElanasPrayer(), new IncandescentDragon(), new KelHolyMarksman(), new MoriaeEncomium(), new WhitefangTemple(),
            new BellringerAngel(), new Chronos(), new EphemeraAngelicSlacker(), new GenesisOfLegend(), new GrimnirWarCyclone(), new Urd(),
            new WellOfDestination(), new AmethystGiant(), new AncientAmplifier(), new ArtifactCall(), new Cucouroux(), new ElectromagicalRhino(),
            new MortonTheManipulator(), new VengefulPuppeteerNoah(), new BladeMage(), new ChaosWielder(), new Chimera(), new ConjuringForce(),
            new KaleidoscopicGlow(), new MagicOwl(), new RaioOmenOfTruth(), new Rimewind(), new RuneBladeSummoner(), new TimewornMageLevi(),
            new DemonlordEachtar(), new MischievousSpirit(), new MordecaiTheDuelist(), new NecroAssassin(), new PrinceCatacomb(),
            new ShadowReaper(), new UnderworldWatchmanKhawy(), new Assassin(), new Cuhullin(), new Leonidas(), new Magnolia(),
            new WeatheredVanguard(), new StonetuskBoar(), new CallOfCocytus(), new Ragnaros(), new LathamHonorableKnight(), new ForestArchelon(),
            new KingElephant());

    @Override
    public CardSet getCards() {
        return new CardSet(PLAYABLE_SET);
    }
}
