package server.card.cardset;

import java.io.*;
import java.util.*;

import server.*;
import server.card.*;

/**
 * ConstructedDeck is a class that deals with playermade decks, which can be
 * serialized/deserialized to store decklists
 * 
 * A ConstructedDeck object is just a map from card ids to card count
 * 
 * @author Michael
 *
 */

public class ConstructedDeck implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;

    public static final int MAX_SIZE = 40, MAX_DUPES = 3;

    /**
     * The working storage for a player's decks
     */
    public static ArrayList<ConstructedDeck> decks = new ArrayList<>();

    /**
     * The map between a card class and its count in the deck
     */
    private final Map<CardText, Integer> cardTextCounts = new HashMap<>();

    /**
     * The name of the deck
     */
    public String name = "New Deck";

    /**
     * The craft of the deck
     */
    public final ClassCraft craft;

    /**
     * A variable keeping track of how many cards there are so we don't have to
     * iterate through the map every time we want to know how big the deck is
     */
    private int count;

    /**
     * Default constructor
     */
    public ConstructedDeck(ClassCraft craft) {
        this.craft = craft;
    }

    /**
     * Accessor for the size of the deck
     * 
     * @return the size of the deck
     */
    public int getSize() {
        return this.count;
    }

    /**
     * Create and return a copy of the deck
     * 
     * @return the copy of the deck
     */
    public ConstructedDeck copy() {
        ConstructedDeck ret = new ConstructedDeck(this.craft);
        ret.copyFrom(this);
        return ret;
    }

    /**
     * Set a deck to a copy of another deck
     * 
     * @param other the deck to copy
     */
    public void copyFrom(ConstructedDeck other) {
        this.cardTextCounts.clear();
        this.cardTextCounts.putAll(other.cardTextCounts);
        this.name = other.name;
        this.count = other.count;
    }

    /**
     * Add multiple copies of a card to this deck
     * 
     * @param cardText the cardtext of the card to add
     * @param count the amount of cards
     * @param respectLimit whether to respect the max dupe limit
     * @return whether or not the operation was successful
     */
    public boolean addCard(CardText cardText, int count, boolean respectLimit) {
        int realCount = respectLimit ? Math.min(count, MAX_DUPES - this.getCountOf(cardText)) : count;
        if (realCount == 0) {
            return false;
        }
        this.cardTextCounts.put(cardText, this.getCountOf(cardText) + realCount);
        this.count += realCount;
        return true;
    }

    /**
     * Add a card to this deck
     *
     * @param cardText the cardtext of the card to add
     * @param respectLimit whether to respect the max dupe limit
     * @return whether or not the operation was successful
     */
    public boolean addCard(CardText cardText, boolean respectLimit) {
        return this.addCard(cardText, 1, respectLimit);
    }

    /**
     * Remove multiple copies of a card from this deck
     * 
     * @param cardText the cardtext of the card to remove
     * @param count The number of cards to remove
     * @return if it returns false then you did something special
     */
    public boolean removeCard(CardText cardText, int count) {
        if (!this.cardTextCounts.containsKey(cardText)) {
            return false;
        }
        int realCount = Math.min(count, this.getCountOf(cardText));
        int newCount = this.getCountOf(cardText) - realCount;
        if (newCount <= 0) {
            this.cardTextCounts.remove(cardText);
        } else {
            this.cardTextCounts.put(cardText, newCount);
        }
        this.count -= realCount;
        return true;
    }

    /**
     * Remove a card from this deck
     *
     * @param cardText the cardtext of the card to remove
     * @return if it returns false then you did something special
     */
    public boolean removeCard(CardText cardText) {
        return this.removeCard(cardText, 1);
    }

    /**
     * Retrieve the counts associated with each card, if they are present in
     * this deck (i.e. count must be greater than 0)
     * @return The set of mappings from a card to its count
     */
    public Set<Map.Entry<CardText, Integer>> getCounts() {
        return this.cardTextCounts.entrySet();
    }

    /**
     * Get the number of occurrences of a card in this deck
     *
     * @param cardText The cardText of the card to query
     * @return the number of times that card appears in this deck
     */
    public int getCountOf(CardText cardText) {
        return Objects.requireNonNullElse(this.cardTextCounts.get(cardText), 0);
    }

    /**
     * Method to validate that a deck is playable, also updates the size of the deck
     * just in case there is a discrepancy
     * 
     * @return whether or not the deck is playable
     */
    public boolean validate() {
        this.count = 0;
        boolean dupes = true;
        for (Map.Entry<CardText, Integer> entry : this.cardTextCounts.entrySet()) {
            this.count += entry.getValue();
            if (entry.getValue() > MAX_DUPES) {
                dupes = false;
            }
        }
        return this.count == MAX_SIZE && dupes;
    }

    /**
     * Converts a decklist to a list of actual Card objects ready to be used in game
     * 
     * @param b    the board to add the cards to
     * @return an arraylist of the created cards;
     */
    public List<Card> convertToCards(Board b) {
        ArrayList<Card> cards = new ArrayList<>(this.count);
        for (Map.Entry<CardText, Integer> entry : this.cardTextCounts.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                Card c = entry.getKey().constructInstance(b);
                cards.add(c);
            }
        }
        return cards;
    }

    /**
     * Subtracts the counts of the other deck from this deck, using bag
     * semantics. Modifies the current deck counts.
     * @param other The deck to subtract with
     * @return This deck
     */
    public ConstructedDeck subtract(ConstructedDeck other) {
        for (Map.Entry<CardText, Integer> entry : other.getCounts()) {
            this.removeCard(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Serializes and saves the decks to file
     */
    public static void saveToFile() {
        File f = new File("decks.dat");
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
            obj.writeObject(decks);
            obj.close();
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Deserializes the decks from file and loads them
     */
    @SuppressWarnings("unchecked")
    public static void loadFromFile() {
        File f = new File("decks.dat");
        if (f.exists()) {
            try {
                FileInputStream file = new FileInputStream(f);
                ObjectInputStream obj = new ObjectInputStream(file);
                decks = (ArrayList<ConstructedDeck>) obj.readObject();
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
