package server.card.cardpack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import server.Board;
import server.card.Card;
import server.card.ClassCraft;

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
	private static final long serialVersionUID = 1L;

	public static final int MAX_SIZE = 40, MAX_DUPES = 3;

	/**
	 * The working storage for a player's decks
	 */
	public static ArrayList<ConstructedDeck> decks = new ArrayList<ConstructedDeck>();

	/**
	 * The map between a card id and its count in the deck
	 */
	public Map<Integer, Integer> idcounts = new HashMap<Integer, Integer>();

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
	 * @param other
	 *            the deck to copy
	 */
	public void copyFrom(ConstructedDeck other) {
		this.idcounts.clear();
		this.idcounts.putAll(other.idcounts);
		this.name = other.name;
		this.count = other.count;
	}

	/**
	 * Method for adding a card to the deck
	 * 
	 * @param id
	 *            the id of the card to add
	 * @return whether or not the operation was successful
	 */
	public boolean addCard(int id) {
		if (!this.idcounts.containsKey(id)) {
			this.idcounts.put(id, 1);
		} else if (this.idcounts.get(id) < MAX_DUPES) {
			this.idcounts.put(id, this.idcounts.get(id) + 1);
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
	 * @param id
	 *            the id of the card to remove
	 * @return if it returns false then you did something special
	 */
	public boolean removeCard(int id) {
		if (!this.idcounts.containsKey(id)) {
			return false;
		}
		this.idcounts.put(id, this.idcounts.get(id) - 1);
		if (this.idcounts.get(id) <= 0) {
			this.idcounts.remove(id);
		}
		this.count--;
		return true;
	}

	/**
	 * Method to validate that a deck is playable, also updates the size of the
	 * deck just in case there is a discrepancy
	 * 
	 * @return whether or not the deck is playable
	 */
	public boolean validate() {
		int size = 0;
		boolean dupes = true;
		for (Map.Entry<Integer, Integer> entry : this.idcounts.entrySet()) {
			this.count += entry.getValue();
			if (entry.getValue() > MAX_DUPES) {
				dupes = false;
			}
		}
		this.count = size;
		return size == MAX_SIZE && dupes;
	}

	/**
	 * Converts a decklist to a list of actual Card objects ready to be used in
	 * game
	 * 
	 * @param b
	 *            the board to add the cards to
	 * @param team
	 *            the team of the player owning the cards
	 * @return an arraylist of the created cards;
	 */
	public ArrayList<Card> convertToCards(Board b) {
		ArrayList<Card> cards = new ArrayList<Card>(this.count);
		for (Map.Entry<Integer, Integer> entry : this.idcounts.entrySet()) {
			Constructor constr = null;
			try {
				constr = CardSet.getCardClass(entry.getKey()).getConstructor(Board.class);
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (int i = 0; i < entry.getValue(); i++) {
				try {
					Card c = (Card) constr.newInstance(b);
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
