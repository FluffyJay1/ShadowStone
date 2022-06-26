package server.card.cardset;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import server.UnleashPowerText;
import server.card.*;
import server.card.cardset.basic.*;
import server.card.cardset.indie.ExpansionSetIndie;
import server.card.cardset.moba.ExpansionSetMoba;
import server.card.cardset.standard.ExpansionSetStandard;
import server.card.leader.*;
import server.card.unleashpower.basic.*;

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
public class CardSet implements Iterable<CardText> {
    public static final Supplier<CardSet> UNPLAYABLE_SET = () -> new CardSet(ExpansionSetBasic.UNPLAYABLE_SET, ExpansionSetStandard.UNPLAYABLE_SET,
            ExpansionSetMoba.UNPLAYABLE_SET, ExpansionSetIndie.UNPLAYABLE_SET);
    public static final Supplier<CardSet> PLAYABLE_SET = () -> new CardSet(ExpansionSetBasic.PLAYABLE_SET, ExpansionSetStandard.PLAYABLE_SET,
            ExpansionSetMoba.PLAYABLE_SET, ExpansionSetIndie.PLAYABLE_SET);
    /**
     * A set of card classes
     */
    private final Set<CardText> cardTexts = new HashSet<>();

    /**
     * Default constructor
     */
    public CardSet() {

    }

    /**
     * Constructor for a CardSet using a series of classes
     * 
     * @param cardTexts the cards to construct the set with
     */
    public CardSet(CardText... cardTexts) {
        this.cardTexts.addAll(Arrays.asList(cardTexts));
    }

    public CardSet(Set<CardText> cardTexts) {
        this.cardTexts.addAll(cardTexts);
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
        this.cardTexts.addAll(other.cardTexts);
    }

    /**
     * Adds a card to this set
     * @param toAdd The card to add
     */
    public void add(CardText toAdd) {
        this.cardTexts.add(toAdd);
    }

    /**
     * Filters until only cards that match the crafts are left
     * Does not modify the source cardset
     * 
     * @param crafts the crafts of cards that remain
     * @return the new cardset
     */
    public CardSet filterCraft(ClassCraft... crafts) {
        List<ClassCraft> craftList = Arrays.asList(crafts);
        return new CardSet(this.cardTexts.stream()
                .filter(ct -> craftList.contains(ct.getTooltip().craft))
                .collect(Collectors.toSet()));
    }

    /**
     * Get a craft's respective default unleash power that they start out with
     * 
     * @param craft The craft to get the unleash power of
     * @return The class of the default unleash power
     */
    public static UnleashPowerText getDefaultUnleashPower(ClassCraft craft) {
        return switch (craft) {
            case FORESTROGUE -> new UnleashEmbraceNature();
            case SWORDPALADIN -> new UnleashSharpenSword();
            case RUNEMAGE -> new UnleashImbueMagic();
            case DRAGONDRUID -> new UnleashFeedFervor();
            case SHADOWSHAMAN -> new UnleashBegetUndead();
            case BLOODWARLOCK -> new UnleashTapSoul();
            case HAVENPRIEST -> new UnleashMendWounds();
            case PORTALHUNTER -> new UnleashEchoExistence();
            default -> null;
        };
    }

    public static LeaderText getDefaultLeader(ClassCraft craft) {
        return switch (craft) {
            case FORESTROGUE -> new Arisa();
            case SWORDPALADIN -> new Erika();
            case RUNEMAGE -> new Isabelle();
            case DRAGONDRUID -> new Rowen();
            case SHADOWSHAMAN -> new Luna();
            case BLOODWARLOCK -> new Urias();
            case HAVENPRIEST -> new Eris();
            case PORTALHUNTER -> new Yuwan();
            default -> null;
        };
    }

    @Override
    public Iterator<CardText> iterator() {
        return this.cardTexts.iterator();
    }
}
