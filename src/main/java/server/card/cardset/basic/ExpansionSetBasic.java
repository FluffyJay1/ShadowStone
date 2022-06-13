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
    public static final CardSet UNPLAYABLE_SET = new CardSet(new Fairy(), new Skeleton(), new Knight(), new Puppet(),
            new HolyFalcon(), new HolyflameTiger(), new Spectre(), new Zombie(), new Snowman(), new HeavyKnight(),
            new LeonidasResolve(), new CrimsonSorcery(), new ClayGolem(), new Dragon(), new Ghost(), new TimeOwl(),
            new HolywingDragon(), new AnalyzingArtifact(), new AncientArtifact(), new MysticArtifact(),
            new RadiantArtifact(), new ArtifactRhino());

    public static final CardSet PLAYABLE_SET = new CardSet(new Goblin(), new Fighter(), new LochnLoad(), new Tiny(),
            new WellOfDestination(), new BellringerAngel(), new GenesisOfLegend(), new WoodOfBrambles(), new Baneling(),
            new CursedStone(), new WeatheredVanguard(), new DragonOracle(), new Curate(), new PuppetRoom(), new Beastmaster(),
            new StonetuskBoar(), new BlackenedScripture(), new MordecaiTheDuelist(), new HallowedDogma(), new BeastcallAria(),
            new SiegeTank(), new Batter(), new Chronos(), new Rhinoceroach(), new DemonlordEachtar(), new MoltenGiant(),
            new FatesHand(), new SummonSnow(), new BreathOfTheSalamander(), new Cucouroux(), new TerrorDemon(), new Tanya(),
            new EphemeraAngelicSlacker(), new ShadowReaper(), new Immortal(), new Belphegor(), new NaturesGuidance(),
            new FairyWhisperer(), new DungeoncrawlFairy(), new SukunaBraveAndSmall(), new SylvanJustice(), new BeetleWarrior(),
            new Cassiopeia(), new GlimmeringWings(), new Vanish(), new SellswordLucius(), new Assassin(), new CentaurVanguard(),
            new Cuhullin(), new NavyLieutenant(), new Thief(), new Jeno(), new FrontlineCavalier(), new Magnolia(),
            new Leonidas(), new WholeSouledSwing(), new Insight(), new TimewornMageLevi(), new MagicOwl(), new ConjureGolem(),
            new MagicMissile(), new WindBlast(), new KaleidoscopicGlow(), new RuneBladeSummoner(), new ChaosWielder(),
            new Rimewind(), new ConjuringForce(), new BladeMage(), new FieryEmbrace(), new FlameDestroyer(), new Chimera(),
            new IvoryDragon(), new ScaleboundPlight(), new AielaDragonKnight(), new DragoonScyther(), new GriffonKnight(),
            new DracomancersRites(), new Rahab(), new PyroxeneDragon(), new BlazingBreath(), new VenomousPucewyrm(),
            new CanyonOfTheDragons(), new Fafnir(), new SkullBeast(), new MischievousSpirit(), new SoulConversion(),
            new DemonEater(), new LadyGreyDeathweaver(), new UndyingResentment(), new ZombieParty(), new BoneChimera(),
            new NecroAssassin(), new PrinceCatacomb(), new BanelingBust(), new UnderworldWatchmanKhawy(), new Defile(),
            new AmblingWraith(), new SwarmingWraith(), new BloodPact(), new RazoryClaw(), new EndearingSuccubusLilith(),
            new DemonKey(), new PrisonOfPain(), new DarkGeneral(), new ScarletSabreur(), new Revelation(), new TemptressVampire(),
            new DemonicRam(), new SacredPlea(), new FeatherfallHourglass(), new RabbitHealer(), new MoriaeEncomium(),
            new PriestOfTheCudgel(), new WhitefangTemple(), new ElanasPrayer(), new KelHolyMarksman(), new TribunalOfGoodAndEvil(),
            new Acceleratium(), new MagisteelLion(), new Icarus(), new AugmentationBestowal(), new AncientAmplifier(),
            new DimensionCut(), new ArtifactCall(), new TranquilCog(), new IronforgedFighter(), new MortonTheManipulator(),
            new PuppeteersStrings(), new VengefulPuppeteerNoah(), new ElectromagicalRhino());

    @Override
    public CardSet getCards() {
        return new CardSet(PLAYABLE_SET);
    }
}
