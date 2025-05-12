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
            new HolyFalcon(), new HolyflameTiger(), new Zombie(), new Snowman(), new HeavyKnight(), new CrimsonSorcery(),
            new ClayGolem(), new Dragon(), new Ghost(), new TimeOwl(), new HolywingDragon(), new AnalyzingArtifact(),
            new AncientArtifact(), new MysticArtifact(), new RadiantArtifact(), new NotCoin(), new WhiteBreath(),
            new BlackBreath(), new LesserLich(), new Barong(), new PrimeArtifact(), new MysterianCircle(), new MysterianMissile());

    public static final CardSet PLAYABLE_SET = new CardSet(new Goblin(), new Fighter(), new WoodOfBrambles(), new CursedStone(),
            new DragonOracle(), new Curate(), new PuppetRoom(), new BlackenedScripture(), new HallowedDogma(), new BeastcallAria(),
            new FatesHand(), new SummonSnow(), new BreathOfTheSalamander(), new Tanya(), new NaturesGuidance(), new FairyWhisperer(),
            new SylvanJustice(), new BeetleWarrior(), new GlimmeringWings(), new SellswordLucius(), new CentaurVanguard(),
            new NavyLieutenant(), new Thief(), new Jeno(), new FrontlineCavalier(), new WholeSouledSwing(), new Insight(),
            new ConjureGolem(), new MagicMissile(), new WindBlast(), new FieryEmbrace(), new FlameDestroyer(), new IvoryDragon(),
            new ScaleboundPlight(), new DragoonScyther(), new BlazingBreath(), new SkullBeast(), new SoulConversion(), new DemonEater(),
            new LadyGreyDeathweaver(), new UndyingResentment(), new ZombieParty(), new BoneChimera(), new Defile(), new AmblingWraith(),
            new SwarmingWraith(), new BloodPact(), new RazoryClaw(), new DarkGeneral(), new ScarletSabreur(), new TemptressVampire(),
            new SacredPlea(), new FeatherfallHourglass(), new RabbitHealer(), new PriestOfTheCudgel(), new TribunalOfGoodAndEvil(),
            new Acceleratium(), new MagisteelLion(), new Icarus(), new AugmentationBestowal(), new DimensionCut(), new TranquilCog(),
            new IronforgedFighter(), new PuppeteersStrings(), new PureheartedSinger(), new DarkBladefiend(), new DeathsBreath(),
            new GaluaOfTwoBreaths(), new Conflagration(), new Siegfried(), new ForestWhispers(), new IDidThat(), new BoulderfistOgre(),
            new ItsOkToDie(), new Maahes(), new ElvenPrincessMage(), new LancerOfTheTempest(), new Meteor(), new Quickblader(),
            new AttendantOfTheNight(), new BloodWolf(), new AcolytesLight(), new WaterFairy(), new TenderRabbitHealer(),
            new MysterianKnowledge());

    @Override
    public CardSet getCards() {
        return new CardSet(PLAYABLE_SET);
    }
}
