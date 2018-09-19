package server.card.cardpack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import client.tooltip.TooltipCard;
import server.card.Card;
import server.card.ClassCraft;
import server.card.cardpack.basic.CardSetBasic;
import server.card.leader.Rowen;
import server.card.unleashpower.UnleashImbueMagic;

/**
 * 
 * CardSet is a class that handles set of card ids. For example, CardSet can
 * return the set of cards ids that a player is allowed to put in a Runemage
 * deck. If a card generated random 2-drops in proper Hearthstone fashion,
 * CardSet would be able to return the set of cards ids that fit that criteria.
 * 
 * A CardSet object just represents a collection of card ids. Various methods
 * can be used on this CardSet object to filter the set until the desired set is
 * achieved.
 * 
 * The intention is that this class will make it easy to add new cards into the
 * game by serving as an interface between the game and the different cards in
 * the game.
 * 
 * Also i don't know how to document code
 * 
 * @author Michael
 *
 */
public class CardSet {
	public static final CardSet SET = new CardSet(CardSetBasic.SET);
	public static final CardSet PLAYABLE_SET = new CardSet(CardSetBasic.PLAYABLE_SET);
	/**
	 * A set of card ids
	 */
	public Set<Integer> ids = new HashSet<Integer>();

	/**
	 * Default constructor
	 */
	public CardSet() {

	}

	/**
	 * Constructor for a CardSet using a series of ids
	 * 
	 * @param ids
	 *            the ids to construct the set with
	 */
	public CardSet(Integer... ids) {
		this.ids.addAll(Arrays.asList(ids));
	}

	/**
	 * Constructor for a CardSet using other CardSets
	 * 
	 * @param sets
	 *            the other sets to construct the set with
	 */
	public CardSet(CardSet... sets) {
		for (CardSet set : sets) {
			this.add(set);
		}
	}

	/**
	 * Adds a set to this set
	 * 
	 * @param other
	 *            the other CardSet to add
	 */
	public void add(CardSet other) {
		this.ids.addAll(other.ids);
	}

	/**
	 * Filters until only cards that match the crafts are left
	 * 
	 * @param crafts
	 *            the crafts of cards that remain
	 * @return the modified cardset
	 */
	public CardSet filterCraft(ClassCraft... crafts) {
		Predicate<Integer> notmatch = i -> {
			return !Arrays.asList(crafts).contains(getCardTooltip(i).craft);
		};
		this.ids.removeIf(notmatch);
		return this;
	}

	/**
	 * Maps a card id to its class
	 * 
	 * @param id
	 *            the id of the card
	 * @return the class of the card
	 */
	public static Class<? extends Card> getCardClass(int id) {
		// ids -1 to -8 reserved for leaders
		// ids -9 to -16 reserved for unleash powers
		if (id >= CardSetBasic.MIN_ID && id <= CardSetBasic.MAX_ID) {
			return CardSetBasic.getCardClass(id);
		}
		switch (id) {
		case UnleashImbueMagic.ID:
			return UnleashImbueMagic.class;
		case Rowen.ID:
			return Rowen.class;
		default:
			return null;
		}

	}

	public static TooltipCard getCardTooltip(int id) {
		Class<? extends Card> c = getCardClass(id);
		try {
			return (TooltipCard) c.getField("TOOLTIP").get(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
