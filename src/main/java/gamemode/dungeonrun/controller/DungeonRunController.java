package gamemode.dungeonrun.controller;

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
import server.card.cardset.basic.ExpansionSetBasic;
import server.card.leader.Rowen;
import utils.WeightedRandomSampler;
import utils.WeightedSampler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DungeonRunController {
    private static final String SAVE_PATH = "dungeonrun.dat";
    public static Run run;
    private static final int LOOT_ENEMY_ROUNDS = 1;
    private static final int LOOT_CLASS_ROUNDS = 2;
    private static final int LOOT_NUM_OPTIONS = 3;
    private static final int LOOT_CARDS_PER_OPTION = 2;
    private static final int DISCARD_ROUNDS = 2;
    private static final int DISCARD_NUM_OPTIONS = 2;
    private static final int DISCARD_CARDS_PER_OPTION = 2;

    public static void generateRun(ClassCraft starterCraft) {
        run = new Run();
        run.state = RunState.PENDING;
        // generate player
        // TODO change from Rowen
        run.player = new Contestant(new Rowen(), CardSet.getDefaultUnleashPower(starterCraft), getDeckForClass(starterCraft), 0, List.of());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File("res/data/dungeonrun/enemies.xml"));
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
                ConstructedDeck deck = getDeckForSpec(spec);
                run.enemies.add(new Contestant(spec.leaderText, spec.unleashPowerText, deck, level, spec.cards));
                level++;
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        saveToFile();
    }

    private static ConstructedDeck getDeckForClass(ClassCraft craft) {
        ConstructedDeck deck = new ConstructedDeck(craft);
        deck.name = "Dungeon Run Deck";
        ExpansionSetBasic basic = new ExpansionSetBasic();
        for (CardText ct : basic.getCards().filterCraft(ClassCraft.NEUTRAL, craft)) {
            deck.addCard(ct, ConstructedDeck.MAX_DUPES, false);
        }
        return deck;
    }

    private static ConstructedDeck getDeckForSpec(EnemySpec spec) {
        // bruh
        ConstructedDeck deck = new ConstructedDeck(spec.leaderText.getTooltip().craft);
        deck.name = "Dungeon Run Deck";
        for (CardText ct : new CardSet(spec.sets).filterCraft(ClassCraft.NEUTRAL, deck.craft)) {
            deck.addCard(ct, ConstructedDeck.MAX_DUPES, false);
        }
        for (CardText ct : spec.cards) {
            deck.addCard(ct, ConstructedDeck.MAX_DUPES, false);
        }
        return deck;
    }

    public static Thread startGame(DataStream dsserver, int localteam) {
        DungeonRunGameRunner runner = new DungeonRunGameRunner(dsserver, localteam, run.player, run.enemies.get(run.current));
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
                determineLootOptions();
                determineDiscardOptions();
            }
        } else {
            run.state = RunState.LOST;
        }
        saveToFile();
    }

    // end looting and move on
    public static void endLooting(List<Integer> lootChoices, List<Integer> discardChoices) {
        run.current++;
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

    private static void determineLootOptions() {
        // We hardcode 2 loot round types: first one steals from opponent's deck, second is from your own class
        run.lootOptions = new ArrayList<>(LOOT_ENEMY_ROUNDS + LOOT_CLASS_ROUNDS);
        // choose cards from the opponent's deck
        // we do a little random sampling
        ConstructedDeck enemyDeck = run.enemies.get(run.current).deck;
        run.lootOptions.addAll(sampleOptionsFromDeck(enemyDeck, LOOT_ENEMY_ROUNDS, LOOT_NUM_OPTIONS, LOOT_CARDS_PER_OPTION,
                DungeonRunController::getEnemyLootRarityWeight));
        // for class-specific loot
        for (int i = 0; i < LOOT_CLASS_ROUNDS; i++) {
            WeightedSampler<CardText> classLootSampler = new WeightedRandomSampler<>();
            CardSet classCards = new CardSet(CardSet.PLAYABLE_SET).filterCraft(ClassCraft.NEUTRAL, run.player.deck.craft);
            for (CardText ct : classCards) {
                classLootSampler.add(ct, getClassLootRarityWeight(ct.getTooltip().rarity));
            }
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
            case GOLD -> 4;
            case LEGENDARY -> 6;
        };
    }

    // when choosing cards from own class
    private static double getClassLootRarityWeight(CardRarity rarity) {
        return switch (rarity) {
            case BRONZE -> 4;
            case SILVER -> 3;
            case GOLD -> 2;
            case LEGENDARY -> 1;
        };
    }

    private static double getDiscardRarityWeight(CardRarity rarity) {
        return switch (rarity) {
            case BRONZE -> 7;
            case SILVER -> 6;
            case GOLD -> 5;
            case LEGENDARY -> 4;
        };
    }

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

        } else {
            try {
                f.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
