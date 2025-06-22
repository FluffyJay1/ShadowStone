package server.card.cardset.standard;

import server.card.cardset.CardSet;
import server.card.cardset.ExpansionSet;
import server.card.cardset.standard.bloodwarlock.*;
import server.card.cardset.standard.dragondruid.*;
import server.card.cardset.standard.forestrogue.*;
import server.card.cardset.standard.havenpriest.*;
import server.card.cardset.standard.neutral.*;
import server.card.cardset.standard.portalshaman.*;
import server.card.cardset.standard.runemage.*;
import server.card.cardset.standard.shadowdeathknight.*;
import server.card.cardset.standard.swordpaladin.*;

public class ExpansionSetStandard extends ExpansionSet {
    public static final CardSet UNPLAYABLE_SET = new CardSet(new ArtifactRhino(), new Camieux(), new LeonidasResolve(),
            new ServantOfDarkness(), new CelestialShikigami(), new DemonicShikigami(), new PaperShikigami(), new ScarabBeetle(),
            new Stegodon());

    public static final CardSet PLAYABLE_SET = new CardSet(new Belphegor(), new DemonicRam(), new DemonKey(), new EndearingSuccubusLilith(),
            new MoltenGiant(), new PrisonOfPain(), new Revelation(), new TerrorDemon(), new AielaDragonKnight(), new CanyonOfTheDragons(),
            new DracomancersRites(), new AbyssalEnforcer(), new GriffonKnight(), new PyroxeneDragon(), new Rahab(), new VenomousPucewyrm(),
            new AncientForestDragon(), new Cassiopeia(), new DungeoncrawlFairy(), new Rhinoceroach(), new SukunaBraveAndSmall(), new Vanish(),
            new ElanasPrayer(), new IncandescentDragon(), new KelHolyMarksman(), new MoriaeEncomium(), new WhitefangTemple(),
            new BellringerAngel(), new Chronos(), new EphemeraAngelicSlacker(), new GenesisOfLegend(), new GrimnirWarCyclone(), new Urd(),
            new WellOfDestination(), new AmethystGiant(), new AncientAmplifier(), new ArtifactCall(), new Cucouroux(), new ElectromagicalRhino(),
            new MortonTheManipulator(), new VengefulPuppeteerNoah(), new BladeMage(), new ChaosWielder(), new Chimera(), new ConjuringForce(),
            new KaleidoscopicGlow(), new MagicOwl(), new RaioOmenOfTruth(), new Rimewind(), new RuneBladeSummoner(), new TimewornMageLevi(),
            new DemonlordEachtar(), new MischievousSpirit(), new MordecaiTheDuelist(), new NecroAssassin(), new PrinceCatacomb(),
            new ShadowReaper(), new UnderworldWatchmanKhawy(), new Assassin(), new Cuhullin(), new Leonidas(), new Magnolia(),
            new WeatheredVanguard(), new StonetuskBoar(), new CallOfCocytus(), new Ragnaros(), new LathamHonorableKnight(), new ForestArchelon(),
            new KingElephant(), new Okami(), new OmnisPrimeOkami(), new KuonFounderOfOnmyodo(), new CurseCrafter(), new Ouroboros(),
            new FrostfireDragon(), new LurchingCorpse(), new Nephthys(), new DiabolusAgito(), new DarkAirjammer(), new SnarlingChains(),
            new MaelstromSerpent(), new DiabolicDrain(), new DireBond(), new Baphomet(), new RedHotBoots(), new DevourerOfHeavens(),
            new ForgottenSanctuary(), new ThemisDecree(), new LionOfTheGoldenCity(), new Tenko(), new GarudaRulerOfStorms(), new RadianceAngel(),
            new Aegina(), new MugnierPurifyingLight(), new MechWingSwordsman(), new CaptivatingConductor(), new ThatWhichErases(),
            new TylleTheWorldgate(), new MiriamSyntheticBeing(), new KnowerOfHistory(), new DyneMasterSwordsman(), new SkyGladiator(),
            new InesMaidenOfClouds(), new DarkDragoonForte(), new DragonewtScholar(), new LordAtomy(), new CaptainLecia(), new ElfGirlLiza(),
            new LeafMan(), new GravekeeperSonia(), new CharlottaTinyJustice(), new AbyssalEnforcer(), new SpreadingPlague(), new BlinkFox(),
            new NorthshireCleric(), new Doomsayer(), new Devolve(), new Blizzard(), new ThreadsOfDespair(), new SpikeridgedSteed(), new CabalistsTome(),
            new FirelandsPortal(), new ColdGuard());

    @Override
    public CardSet getCards() {
        return new CardSet(PLAYABLE_SET);
    }
}
