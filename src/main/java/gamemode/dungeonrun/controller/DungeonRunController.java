package gamemode.dungeonrun.controller;

import gamemode.dungeonrun.Passive;
import gamemode.dungeonrun.model.Contestant;
import gamemode.dungeonrun.model.EnemySpec;
import gamemode.dungeonrun.model.Run;
import gamemode.dungeonrun.model.RunState;
import network.DataStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import server.card.CardRarity;
import server.card.CardText;
import server.card.ClassCraft;
import server.card.cardset.CardSet;
import server.card.cardset.ConstructedDeck;
import server.card.cardset.ExpansionSet;
import server.card.cardset.anime.*;
import server.card.cardset.basic.*;
import server.card.cardset.basic.bloodwarlock.*;
import server.card.cardset.basic.dragondruid.*;
import server.card.cardset.basic.forestrogue.*;
import server.card.cardset.basic.havenpriest.*;
import server.card.cardset.basic.neutral.*;
import server.card.cardset.basic.portalshaman.*;
import server.card.cardset.basic.runemage.*;
import server.card.cardset.basic.shadowdeathknight.*;
import server.card.cardset.basic.swordpaladin.*;
import server.card.cardset.indie.*;
import server.card.cardset.moba.*;
import server.card.cardset.special.treasure.Treasures;
import server.card.cardset.standard.*;
import server.card.cardset.standard.bloodwarlock.*;
import server.card.cardset.standard.dragondruid.*;
import server.card.cardset.standard.forestrogue.*;
import server.card.cardset.standard.havenpriest.*;
import server.card.cardset.standard.neutral.*;
import server.card.cardset.standard.portalshaman.*;
import server.card.cardset.standard.runemage.*;
import server.card.cardset.standard.shadowdeathknight.*;
import server.card.cardset.standard.swordpaladin.*;
import utils.SelectRandom;
import utils.WeightedRandomSampler;
import utils.WeightedSampler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DungeonRunController {
    private static final String SAVE_PATH = "dungeonrun.dat";
    public static Run run;
    private static final int PASSIVE_ROUNDS = 1;
    private static final int PASSIVE_NUM_OPTIONS = 3;
    private static final List<Integer> PASSIVE_OFFER_STAGES = List.of(0, 2, 4);
    private static final int TREASURE_ROUNDS = 1;
    private static final int TREASURE_NUM_OPTIONS = 3;
    private static final int TREASURE_CARDS_PER_OPTION = 1;
    private static final List<Integer> TREASURE_OFFER_STAGES = List.of(1, 3, 5);
    private static final int LOOT_ENEMY_ROUNDS = 1;
    private static final int LOOT_CLASS_ROUNDS = 2;
    private static final int LOOT_NUM_OPTIONS = 3;
    private static final int LOOT_CARDS_PER_OPTION = 3;
    private static final int DISCARD_ROUNDS = 1;
    private static final int DISCARD_NUM_OPTIONS = 2;
    private static final int DISCARD_CARDS_PER_OPTION = 3;
    private static final int ENEMY_DECK_SAMPLE_SIZE = 2;
    private static final int ENEMY_DECK_SAMPLE_SIZE_PER_LEVEL = 3;
    private static final int ENEMY_DECK_SAMPLE_ROUNDS = 3;

    private static final Map<ExpansionSet, Double> EXPANSION_LOOT_WEIGHTS = new HashMap<>() {{
        put(new ExpansionSetBasic(), 1.);
        put(new ExpansionSetStandard(), 1.);
        put(new ExpansionSetIndie(), 1.5);
        put(new ExpansionSetMoba(), 1.5);
        put(new ExpansionSetAnime(), 1.5);
    }};

    public static void generateRun(ClassCraft starterCraft) {
        run = new Run();
        run.state = RunState.PENDING;
        // generate player
        run.player = new Contestant(CardSet.getDefaultLeader(starterCraft), CardSet.getDefaultUnleashPower(starterCraft), getDeckForClass(starterCraft), 3, List.of(), List.of());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(DungeonRunController.class.getClassLoader().getResourceAsStream("data/dungeonrun/enemies.xml"));
            doc.getDocumentElement().normalize();
            // get list of <enemy>
            // parse them into EnemySpecs
            NodeList enemyNodes = doc.getElementsByTagName("contestant");
            List<EnemySpec> enemySpecs = new ArrayList<>(enemyNodes.getLength());
            for (int i = 0; i < enemyNodes.getLength(); i++) {
                Node enemyNode = enemyNodes.item(i);
                if (enemyNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element enemyElement = (Element) enemyNode;
                    enemySpecs.add(new EnemySpec(enemyElement));
                }
            }
            // evaluate the specs in random order
            run.enemies = new ArrayList<>(enemySpecs.size());
            int level = 0;
            while (!enemySpecs.isEmpty()) {
                EnemySpec spec = enemySpecs.remove((int) (Math.random() * enemySpecs.size()));
                ConstructedDeck deck = getDeckForSpec(spec, level);
                List<Passive> passives = generatePassivesForEnemyLevel(level);
                run.enemies.add(new Contestant(spec.leaderText, spec.unleashPowerText, deck, level, spec.cards, passives));
                level++;
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        saveToFile();
    }

    private static List<CardText> getStarterCards(ClassCraft craft) {
        return switch(craft) {
            case NEUTRAL -> List.of(new Goblin(), new Goblin(), new Fighter(), new Fighter(), new PureheartedSinger(), new IDidThat(), new GrimnirWarCyclone(), new CallOfCocytus(), new BoulderfistOgre(), new BoulderfistOgre());
            case BLOODWARLOCK -> List.of(new AmblingWraith(), new BloodPact(), new BloodWolf(), new DarkGeneral(), new RazoryClaw(), new AbyssalEnforcer(), new ScarletSabreur(), new TemptressVampire(), new MoltenGiant(), new SnarlingChains());
            case DRAGONDRUID -> List.of(new BlazingBreath(), new Conflagration(), new DragonOracle(), new DragoonScyther(), new GaluaOfTwoBreaths(), new IvoryDragon(), new Siegfried(), new SpreadingPlague(), new CanyonOfTheDragons(), new AielaDragonKnight());
            case FORESTROGUE -> List.of(new SylvanJustice(), new ElvenPrincessMage(), new FairyWhisperer(), new ForestWhispers(), new NaturesGuidance(), new WaterFairy(), new WoodOfBrambles(), new BlinkFox(), new AncientForestDragon(), new Okami());
            case HAVENPRIEST -> List.of(new AcolytesLight(), new BeastcallAria(), new BlackenedScripture(), new Curate(), new HallowedDogma(), new MindControl(), new SacredPlea(), new NorthshireCleric(), new GravekeeperSonia(), new WhitefangTemple());
            case PORTALSHAMAN -> List.of(new DimensionCut(), new TranquilCog(), new IronforgedFighter(), new MagisteelLion(), new PuppeteersStrings(), new PuppetRoom(), new Substitution(), new Devolve(), new VengefulPuppeteerNoah(), new TylleTheWorldgate());
            case RUNEMAGE -> List.of(new Insight(), new ConjureGolem(), new FatesHand(), new MagicMissile(), new FlameDestroyer(), new SummonSnow(), new WindBlast(), new FirelandsPortal(), new Blizzard(), new CabalistsTome(), new KuonFounderOfOnmyodo());
            case SHADOWDEATHKNIGHT -> List.of(new BoneChimera(), new DemonEater(), new DarkBladefiend(), new LadyGreyDeathweaver(), new SkullBeast(), new SoulConversion(), new UndyingResentment(), new NecroAssassin(), new ThreadsOfDespair(), new MordecaiTheDuelist());
            case SWORDPALADIN -> List.of(new CentaurVanguard(), new Equality(), new FrontlineCavalier(), new Jeno(), new Quickblader(), new Thief(), new WholeSouledSwing(), new SpikeridgedSteed(), new WeatheredVanguard(), new Leonidas());
        };
    }

    private static ConstructedDeck getDeckForClass(ClassCraft craft) {
        ConstructedDeck deck = new ConstructedDeck(craft);
        deck.name = "Dungeon Run Deck";
        for (CardText ct : getStarterCards(craft)) {
            deck.addCard(ct, false);
        }
        for (CardText ct : getStarterCards(ClassCraft.NEUTRAL)) {
            deck.addCard(ct, false);
        }
        return deck;
    }

    private static ConstructedDeck getDeckForSpec(EnemySpec spec, int level) {
        // bruh
        ConstructedDeck deck = new ConstructedDeck(spec.leaderText.getTooltip().craft);
        deck.name = "Dungeon Run Deck";
        for (CardText ct : getStarterCards(deck.craft)) {
            deck.addCard(ct, false);
        }
        WeightedSampler<CardText> extraCardSampler = new WeightedRandomSampler<>();
        for (CardText ct : spec.sets.filterCraft(ClassCraft.NEUTRAL, deck.craft)) {
            extraCardSampler.add(ct, getEnemyDeckRarityWeight(ct.getTooltip().rarity, level));
        }
        for (int i = 0; i < ENEMY_DECK_SAMPLE_ROUNDS; i++) {
            List<CardText> topOptions = extraCardSampler.sample();
            for (CardText ct : topOptions.subList(0, Math.min(ENEMY_DECK_SAMPLE_SIZE + ENEMY_DECK_SAMPLE_SIZE_PER_LEVEL * level, topOptions.size()))) {
                deck.addCard(ct, false);
            }
        }
        for (CardText ct : spec.cards) {
            deck.addCard(ct, Math.min(ConstructedDeck.MAX_DUPES, level + 1), false);
        }
        return deck;
    }

    private static List<Passive> generatePassivesForEnemyLevel(int level) {
        return SelectRandom.from(Passive.ALL, level / 2);
    }

    public static Thread startGame(DataStream dsserver) {
        DungeonRunGameRunner runner = new DungeonRunGameRunner(dsserver, run.player, run.enemies.get(run.current));
        Thread t = new Thread(runner);
        t.start();
        run.state = RunState.IN_PROGRESS;
        saveToFile();
        return t;
    }

    public static void endGame(boolean win) {
        if (win) {
            if (run.current == run.enemies.size() - 1) {
                // if last enemy, win game
                run.state = RunState.WON;
            } else {
                run.state = RunState.LOOTING;
                determinePassiveOptions();
                determineTreasureOptions();
                determineLootOptions();
                determineDiscardOptions();
            }
        } else {
            run.state = RunState.LOST;
        }
        saveToFile();
    }

    // end looting and move on
    public static void endLooting(List<Integer> passiveChoices, List<Integer> treasureChoices, List<Integer> lootChoices, List<Integer> discardChoices) {
        run.current++;
        for (int round = 0; round < passiveChoices.size(); round++) {
            int choice = passiveChoices.get(round);
            List<Passive> option = run.passiveOptions.get(round).get(choice);
            run.player.passives.addAll(option);
        }
        for (int round = 0; round < treasureChoices.size(); round++) {
            int choice = treasureChoices.get(round);
            List<CardText> option = run.treasureOptions.get(round).get(choice);
            for (CardText ct : option) {
                run.player.deck.addCard(ct, false);
            }
        }
        for (int round = 0; round < lootChoices.size(); round++) {
            int choice = lootChoices.get(round);
            List<CardText> option = run.lootOptions.get(round).get(choice);
            for (CardText ct : option) {
                run.player.deck.addCard(ct, false);
            }
        }
        for (int round = 0; round < discardChoices.size(); round++) {
            int choice = discardChoices.get(round);
            List<CardText> option = run.discardOptions.get(round).get(choice);
            for (CardText ct : option) {
                run.player.deck.removeCard(ct);
            }
        }
        run.lootOptions = null;
        run.discardOptions = null;
        run.state = RunState.PENDING;
        saveToFile();
    }

    public static void endRun() {
        File f = new File(SAVE_PATH);
        if (f.exists()) {
            f.delete();
        }
        run = null;
    }

    private static void determinePassiveOptions() {
        run.passiveOptions = new ArrayList<>(PASSIVE_ROUNDS);
        if (PASSIVE_OFFER_STAGES.contains(run.current)) {
            for (int i = 0; i < PASSIVE_ROUNDS; i++) {
                List<Passive> choices = SelectRandom.from(Passive.ALL, PASSIVE_NUM_OPTIONS);
                List<List<Passive>> round = choices.stream().map(List::of).toList(); // yuh
                run.passiveOptions.add(round);
            }
        }
    }

    private static void determineTreasureOptions() {
        run.treasureOptions = new ArrayList<>(TREASURE_ROUNDS);
        if (TREASURE_OFFER_STAGES.contains(run.current)) {
            WeightedSampler<CardText> treasureSampler = new WeightedRandomSampler<>();
            for (CardText ct : Treasures.CARDS) {
                treasureSampler.add(ct, 1); // todo weight treasures differently?
            }
            for (int i = 0; i < TREASURE_ROUNDS; i++) {
                List<CardText> topOptions = treasureSampler.sample();
                run.treasureOptions.add(distributeOptions(topOptions, TREASURE_NUM_OPTIONS, TREASURE_CARDS_PER_OPTION));
            }
        }
    }

    private static void determineLootOptions() {
        // We hardcode 2 loot round types: first one steals from opponent's deck, second is from your own class
        run.lootOptions = new ArrayList<>(LOOT_ENEMY_ROUNDS + LOOT_CLASS_ROUNDS);
        // choose cards from the opponent's deck
        // we do a little random sampling
        ConstructedDeck enemyDeck = run.enemies.get(run.current).deck;
        run.lootOptions.addAll(sampleOptionsFromDeck(enemyDeck, LOOT_ENEMY_ROUNDS, LOOT_NUM_OPTIONS, LOOT_CARDS_PER_OPTION,
                DungeonRunController::getEnemyLootRarityWeight));
        // for class-specific loot
        WeightedSampler<CardText> classLootSampler = new WeightedRandomSampler<>();
        for (Map.Entry<ExpansionSet, Double> expansionWeightEntry : EXPANSION_LOOT_WEIGHTS.entrySet()) {
            CardSet classCards = expansionWeightEntry.getKey().getCards().filterCraft(ClassCraft.NEUTRAL, run.player.deck.craft);
            for (CardText ct : classCards) {
                classLootSampler.add(ct, getClassLootRarityWeight(ct.getTooltip().rarity) * expansionWeightEntry.getValue());
            }
        }
        for (int i = 0; i < LOOT_CLASS_ROUNDS; i++) {
            List<CardText> topOptions = classLootSampler.sample();
            run.lootOptions.add(distributeOptions(topOptions, LOOT_NUM_OPTIONS, LOOT_CARDS_PER_OPTION));
        }
    }

    private static void determineDiscardOptions() {
        // chose some number of cards from own deck to discard
        run.discardOptions = new ArrayList<>(DISCARD_ROUNDS);
        ConstructedDeck playerDeck = run.player.deck;
        run.discardOptions.addAll(sampleOptionsFromDeck(playerDeck, DISCARD_ROUNDS, DISCARD_NUM_OPTIONS, DISCARD_CARDS_PER_OPTION,
                DungeonRunController::getDiscardRarityWeight));
    }

    // When sampling options from a deck, we should try to ensure that no two options are the same
    // however, if the deck has enough cards, we should allow the same option to appear in different rounds of selection
    private static List<List<List<CardText>>> sampleOptionsFromDeck(ConstructedDeck deck, int numRounds, int numOptions, int cardsPerOption,
                                                                    Function<CardRarity, Double> weightfn) {
        List<List<List<CardText>>> ret = new ArrayList<>(numRounds);
        // deck to keep track of what's remaining in the deck as if the player chose all options
        ConstructedDeck copy = deck.copy();
        for (int i = 0; i < numRounds; i++) {
            WeightedSampler<CardText> deckSampler = new WeightedRandomSampler<>();
            for (Map.Entry<CardText, Integer> entry : copy.getCounts()) {
                CardText cardText = entry.getKey();
                double weight = weightfn.apply(cardText.getTooltip().rarity) * entry.getValue();
                deckSampler.add(cardText, weight);
            }
            List<CardText> topOptions = deckSampler.sample();
            List<List<CardText>> options = distributeOptions(topOptions, numOptions, cardsPerOption);
            ret.add(options);
            // pretend the player has removed all possible cards, before deciding on what to show for the next round
            for (List<CardText> lct : options) {
                for (CardText ct : lct) {
                    copy.removeCard(ct);
                }
            }
        }
        return ret;
    }

    private static List<List<CardText>> distributeOptions(List<CardText> topOptions, int numOptions, int cardsPerOption) {
        List<List<CardText>> ret = new ArrayList<>(numOptions);
        for (int i = 0; i < numOptions; i++) {
            List<CardText> option = new ArrayList<>(cardsPerOption);
            for (int j = 0; j < cardsPerOption; j++) {
                int targetInd = j * numOptions + i;
                if (targetInd < topOptions.size()) {
                    option.add(topOptions.get(targetInd));
                }
            }
            ret.add(option);
        }
        return ret;
    }

    // when stealing cards from the enemy deck after winning
    private static double getEnemyLootRarityWeight(CardRarity rarity) {
        return switch (rarity) {
            case BRONZE -> 1;
            case SILVER -> 2;
            case GOLD -> 5;
            case LEGENDARY -> 10;
        };
    }

    // when choosing cards from own class
    private static double getClassLootRarityWeight(CardRarity rarity) {
        return switch (rarity) {
            case BRONZE -> 5;
            case SILVER -> 4;
            case GOLD -> 3;
            case LEGENDARY -> 2;
        };
    }

    private static double getDiscardRarityWeight(CardRarity rarity) {
        return switch (rarity) {
            case BRONZE -> 8;
            case SILVER -> 6;
            case GOLD -> 5;
            case LEGENDARY -> 4;
        };
    }

    // when deciding the cards that go into the enemy's deck at the start of the run
    private static double getEnemyDeckRarityWeight(CardRarity rarity, int level) {
        return switch (rarity) {
            case BRONZE -> 5;
            case SILVER -> 4 + level * 0.25;
            case GOLD -> 2 + level * 0.75;
            case LEGENDARY -> 1 + level * 1.25;
        };
    }

    // when choosing cards from own class
    /**
     * Serializes and saves the decks to file
     */
    public static void saveToFile() {
        File f = new File(SAVE_PATH);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream file = new FileOutputStream(f);
            ObjectOutputStream obj = new ObjectOutputStream(file);
            obj.writeObject(run);
            obj.close();
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Deserializes the run from file and loads them
     */
    public static void loadFromFile() {
        File f = new File(SAVE_PATH);
        if (f.exists()) {
            try {
                FileInputStream file = new FileInputStream(f);
                ObjectInputStream obj = new ObjectInputStream(file);
                run = (Run) obj.readObject();
                // if quit in the middle of a game, the run is considered lost
                if (run.state.equals(RunState.IN_PROGRESS)) {
                   run.state = RunState.LOST;
                }
                obj.close();
                file.close();
            } catch (IOException | ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
}
