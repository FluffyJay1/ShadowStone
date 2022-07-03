package client.tooltip;

import server.Player;

import java.util.List;
import java.util.function.Supplier;

public class Tooltip {
    public static final Tooltip OVERFLOW = new Tooltip("Overflow",
            String.format("<b>Overflow</b> is active for a player when they have %d or more maximum mana.", Player.OVERFLOW_THRESHOLD),
            List::of);
    public static final Tooltip VENGEANCE = new Tooltip("Vengeance",
            String.format("<b>Vengeance</b> is active for a player when they have %d or less health.", Player.VENGEANCE_THRESHOLD),
            List::of);
    public static final Tooltip BATTLECRY = new Tooltip("Battlecry",
            "<b>Battlecry</b> abilities activate whenever the card is played.",
            List::of);
    public static final Tooltip UNLEASH = new Tooltip("Unleash",
            "<b>Unleash</b> abilities on a minion activate after a player uses their Unleash Power on a minion, or when another card triggers them.",
            List::of);
    public static final Tooltip CLASH = new Tooltip("Clash",
            "<b>Clash</b> abilities activate on a minion whenever it attacks or is attacked by another minion.",
            List::of);
    public static final Tooltip LASTWORDS = new Tooltip("Last Words",
            "<b>Last Words</b> abilities activate whenever a card in play is destroyed.",
            List::of);
    public static final Tooltip STORM = new Tooltip("Storm",
            "Minions with <b>Storm</b> can immediately attack on the turn they're played.",
            List::of);
    public static final Tooltip RUSH = new Tooltip("Rush",
            "Minions with <b>Rush</b> can immediately attack enemy minions on the turn they're played.",
            List::of);
    public static final Tooltip WARD = new Tooltip("Ward",
            "If there is a minion with <b>Ward</b>, it must be attacked before targets without <b>Ward</b> can be attacked.",
            List::of);
    public static final Tooltip BANE = new Tooltip("Bane",
            "Minions with <b>Bane</b> automatically destroy the other minion after engaging in combat.",
            List::of);
    public static final Tooltip POISONOUS = new Tooltip("Poisonous",
            "Cards with <b>Poisonous</b> destroy any minion they damage.",
            List::of);
    public static final Tooltip COUNTDOWN = new Tooltip("Countdown",
            "At the start of the player's turn, cards in play with <b>Countdown</b> count down by one. When their <b>Countdown</b> goes to 0, the card is destroyed.",
            List::of);
    public static final Tooltip BLAST = new Tooltip("Blast(X)",
            "Deal X damage to a random enemy minion. If there are no enemy minions in play, deal X damage to the enemy leader.",
            List::of);
    public static final Tooltip AURA = new Tooltip("Aura",
            "<b>Aura</b> effects grant effects to other cards while the source of the aura is in play.",
            List::of);
    public static final Tooltip BANISH = new Tooltip("Banish",
            "Remove a card without destroying it. Will not trigger <b>Last Words</b>.",
            () -> List.of(Tooltip.LASTWORDS));
    public static final Tooltip CHOOSE = new Tooltip("Choose",
            "Cards with <b>Choose</b> effects allow you to choose what happens in either a <b>Battlecry</b> or an <b>Unleash</b>.",
            () -> List.of(Tooltip.BATTLECRY, Tooltip.UNLEASH));
    public static final Tooltip TRANSFORM = new Tooltip("Transform",
            "Replace a card with another card. Any process pertaining to the replaced card is cancelled. Does not count as the card leaving/entering play.",
            List::of);
    public static final Tooltip SHADOW = new Tooltip("Shadow",
            "You gain 1 <b>Shadow</b> whenever one of your cards is destroyed or cast.",
            List::of);
    public static final Tooltip NECROMANCY = new Tooltip("Necromancy(X)",
            "If you have at least X <b>Shadows</b>, consume them to activate a bonus effect.",
            () -> List.of(SHADOW));
    public static final Tooltip SPELLBOOST = new Tooltip("Spellboost",
            "When you play a spell, you <b>Spellboost</b> the cards in your hand. Only certain cards can take advantage of its effects.",
            List::of);
    public static final Tooltip SPEND = new Tooltip("Spend(X)",
            "If you have at least X mana leftover, consume them to activate bonus effects.",
            List::of);
    public static final Tooltip STRIKE = new Tooltip("Strike",
            "<b>Strike</b> effects on a minion activate whenever they attack.",
            List::of);
    public static final Tooltip MINIONSTRIKE = new Tooltip("Minion Strike",
            "<b>Minion Strike</b> effects on a minion activate whenever they attack another minion.",
            List::of);
    public static final Tooltip LEADERSTRIKE = new Tooltip("Leader Strike",
            "<b>Leader Strike</b> effects on a minion activate whenever they attack a leader.",
            List::of);
    public static final Tooltip RETALIATE = new Tooltip("Retaliate",
            "<b>Retaliate</b> effects on a minion activate whenever they are attacked.",
            List::of);
    public static final Tooltip LIFESTEAL = new Tooltip("Lifesteal",
            "After a card with <b>Lifesteal</b> deals damage, it restores health to the owner's leader equal to the damage dealt.",
            List::of);
    public static final Tooltip STEALTH = new Tooltip("Stealth",
            "Cards with <b>Stealth</b> can't be targeted by the enemy for attacks and effects. They lose <b>Stealth</b> after trying to deal damage.",
            List::of);
    public static final Tooltip SHIELD = new Tooltip("Shield(X)",
            "<b>Shield</b> with X health. If the <b>Shield</b> is active, the minion will take no damage, and <b>Shield</b> will absorb the full damage instance. " +
                    "The <b>Shield</b> is removed when it has 0 health.",
            List::of);
    public static final Tooltip REANIMATE = new Tooltip("Reanimate(X)",
            "Randomly summon the highest-cost minion in your graveyard that costs X or less.",
            List::of);
    public static final Tooltip RESONANCE = new Tooltip("Resonance",
            "<b>Resonance</b> is active for a player when they have an even number of cards left in their deck.",
            List::of);
    public static final Tooltip ELUSIVE = new Tooltip("Elusive",
            "Cards with <b>Elusive</b> can't be targeted by enemy spells or effects.",
            List::of);
    public static final Tooltip STALWART = new Tooltip("Stalwart",
            "Can't be explicitly destroyed by effects, <b>Bane</b>, or <b>Poisonous</b>. Can still be destroyed by damage and <b>Countdown</b>.",
            () -> List.of(BANE, POISONOUS, COUNTDOWN));
    public static final Tooltip DISARMED = new Tooltip("Disarmed",
            "Players cannot order <b>Disarmed</b> minions to attack.",
            List::of);
    public static final Tooltip FROZEN = new Tooltip("Frozen",
            "Players cannot order <b>Frozen</b> minions to attack. <b>Frozen</b> minions become <b>Unfrozen</b> at the end of turns where " +
                    "they could have attacked otherwise.",
            List::of);
    public static final Tooltip FREEZING_TOUCH = new Tooltip("Freezing Touch",
            "Cards with <b>Freezing Touch</b> automatically <b>Freeze</b> any minions they damage.",
            () -> List.of(FROZEN));
    public static final Tooltip MUTE = new Tooltip("Mute",
            "Causes effects on a card to not respond, like <b>Unleash</b> and <b>Last Words</b> abilities. " +
                    "Does not remove static traits like stats or <b>Ward</b>.",
            () -> List.of(UNLEASH, LASTWORDS, WARD));

    public final String name;
    public final String description;
    public Supplier<List<Tooltip>> references;

    public Tooltip(String name, String description, Supplier<List<Tooltip>> references) {
        this.name = name;
        this.description = description;
        this.references = references;
    }
}
