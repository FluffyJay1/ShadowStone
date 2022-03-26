package client.tooltip;

import java.util.List;
import java.util.function.Supplier;

public class Tooltip {
    public static final Tooltip OVERFLOW = new Tooltip("Overflow",
            "<b>Overflow</b> is active for a player when they have 7 or more maximum mana.",
            List::of);
    public static final Tooltip VENGEANCE = new Tooltip("Vengeance",
            "<b>Vengeance</b> is active for a player when they have 15 or less health.",
            List::of);
    public static final Tooltip BATTLECRY = new Tooltip("Battlecry",
            "<b>Battlecry</b> abilities activate whenever the card is played.",
            List::of);
    public static final Tooltip UNLEASH = new Tooltip("Unleash",
            "<b>Unleash</b> abilities on a minion activate after a player uses their <b>Unleash Power</b> on a minion, or when another card triggers them.",
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
    public static final Tooltip NECROMANCY = new Tooltip("Necromancy(X)",
            "If you have at least X shadows, consume them to activate a bonus effect.",
            List::of);
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

    public final String name;
    public final String description;
    public Supplier<List<Tooltip>> references;

    public Tooltip(String name, String description, Supplier<List<Tooltip>> references) {
        this.name = name;
        this.description = description;
        this.references = references;
    }
}
