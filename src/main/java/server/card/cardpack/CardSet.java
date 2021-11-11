package server.card.cardpack;

import java.util.*;
import java.util.function.*;

import client.tooltip.*;
import server.card.*;
import server.card.cardpack.basic.*;
import server.card.unleashpower.*;

/**
 * 
 * CardSet is a class that handles set of card classes. For example, CardSet can
 * return the set of cards classes that a player is allowed to put in a Runemage
 * deck. If a card generated random 2-drops in proper Hearthstone fashion,
 * CardSet would be able to return the set of cards classes that fit that
 * criteria.
 * 
 * A CardSet object just represents a collection of card classes. Various
 * methods can be used on this CardSet object to filter the set until the
 * desired set is achieved.
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
     * A set of card classes
     */
    public Set<Class<? extends Card>> cardClasses = new HashSet<>();

    /**
     * Default constructor
     */
    public CardSet() {

    }

    /**
     * Constructor for a CardSet using a series of classes
     * 
     * @param cardClasses the cards to construct the set with
     */
    public CardSet(Class<? extends Card>... cardClasses) {
        this.cardClasses.addAll(Arrays.asList(cardClasses));
    }

    /**
     * Constructor for a CardSet using other CardSets
     * 
     * @param sets the other sets to construct the set with
     */
    public CardSet(CardSet... sets) {
        for (CardSet set : sets) {
            this.add(set);
        }
    }

    /**
     * Adds a set to this set
     * 
     * @param other the other CardSet to add
     */
    public void add(CardSet other) {
        this.cardClasses.addAll(other.cardClasses);
    }

    /**
     * Filters until only cards that match the crafts are left
     * 
     * @param crafts the crafts of cards that remain
     * @return the modified cardset
     */
    public CardSet filterCraft(ClassCraft... crafts) {
        Predicate<Class<? extends Card>> notmatch = cardClass -> {
            return !Arrays.asList(crafts).contains(getCardTooltip(cardClass).craft);
        };
        this.cardClasses.removeIf(notmatch);
        return this;
    }

    /**
     * Maps a card class to its tooltip
     * 
     * @param cardClass the class of the card
     * @return the tooltip of the card
     */
    public static TooltipCard getCardTooltip(Class<? extends Card> cardClass) {
        try {
            return (TooltipCard) cardClass.getField("TOOLTIP").get(null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a craft's respective default unleash power that they start out with
     * 
     * @param craft The craft to get the unleash power of
     * @return The class of the default unleash power
     */
    public static Class<? extends Card> getDefaultUnleashPower(ClassCraft craft) {
        switch (craft) {
        case FORESTROGUE:
            return UnleashEmbraceNature.class;
        case SWORDPALADIN:
            return UnleashSharpenSword.class;
        case RUNEMAGE:
            return UnleashImbueMagic.class;
        case DRAGONDRUID:
            return UnleashFeedFervor.class;
        case SHADOWSHAMAN:
            return UnleashBegetUndead.class;
        case BLOODWARLOCK:
            return UnleashTapSoul.class;
        case HAVENPRIEST:
            return UnleashMendWounds.class;
        case PORTALHUNTER:
            return UnleashEchoExistence.class;
        default:
            return null;
        }
    }
}
