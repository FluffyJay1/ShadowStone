package server.card.cardpack;

import java.io.*;
import java.lang.reflect.*;
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
    /**
     * 
     */
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
    public final Map<Class<? extends Card>, Integer> cardClassCounts = new HashMap<>();

    /**
     * The name of the deck
     */
    public String name = "New Deck";

    /**
     * The craft of the deck
     */
    public ClassCraft craft = ClassCraft.NEUTRAL;

    /**
     * A variable keeping track of how many cards there are so we don't have to
     * iterate through the map every time we want to know how big the deck is
     */
    private int count;

    /**
     * Default constructor
     */
    public ConstructedDeck() {

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
        ConstructedDeck ret = new ConstructedDeck();
        ret.copyFrom(this);
        return ret;
    }

    /**
     * Set a deck to a copy of another deck
     * 
     * @param other the deck to copy
     */
    public void copyFrom(ConstructedDeck other) {
        this.cardClassCounts.clear();
        this.cardClassCounts.putAll(other.cardClassCounts);
        this.name = other.name;
        this.count = other.count;
    }

    /**
     * Method for adding a card to the deck
     * 
     * @param cardClass the class of the card to add
     * @return whether or not the operation was successful
     */
    public boolean addCard(Class<? extends Card> cardClass) {
        if (!this.cardClassCounts.containsKey(cardClass)) {
            this.cardClassCounts.put(cardClass, 1);
        } else if (this.cardClassCounts.get(cardClass) < MAX_DUPES) {
            this.cardClassCounts.put(cardClass, this.cardClassCounts.get(cardClass) + 1);
        } else {
            // ye fucked up
            return false;
        }
        this.count++;
        return true;
    }

    /**
     * Method for removing a card from the deck
     * 
     * @param cardClass the class of the card to remove
     * @return if it returns false then you did something special
     */
    public boolean removeCard(Class<? extends Card> cardClass) {
        if (!this.cardClassCounts.containsKey(cardClass)) {
            return false;
        }
        this.cardClassCounts.put(cardClass, this.cardClassCounts.get(cardClass) - 1);
        if (this.cardClassCounts.get(cardClass) <= 0) {
            this.cardClassCounts.remove(cardClass);
        }
        this.count--;
        return true;
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
        for (Map.Entry<Class<? extends Card>, Integer> entry : this.cardClassCounts.entrySet()) {
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
        for (Map.Entry<Class<? extends Card>, Integer> entry : this.cardClassCounts.entrySet()) {
            Constructor<? extends Card> constr = null;
            try {
                constr = entry.getKey().getConstructor(Board.class);
            } catch (NoSuchMethodException | SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            for (int i = 0; i < entry.getValue(); i++) {
                try {
                    assert constr != null;
                    Card c = constr.newInstance(b);
                    cards.add(c);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return cards;
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
