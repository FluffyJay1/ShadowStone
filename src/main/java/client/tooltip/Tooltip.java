package client.tooltip;

public class Tooltip {
    public static final Tooltip OVERFLOW = new Tooltip("Overflow",
            "<b>Overflow</b> is active for a player when they have 7 or more maximum mana.");
    public static final Tooltip VENGEANCE = new Tooltip("Vengeance",
            "<b>Vengeance</b> is active for a player when they have 15 or less health.");
    public static final Tooltip BATTLECRY = new Tooltip("Battlecry",
            "<b>Battlecry</b> abilities activate whenever the card is played.");
    public static final Tooltip UNLEASH = new Tooltip("Unleash",
            "<b>Unleash</b> abilities on a minion activate after a player uses their <b>Unleash Power</b> on a minion, or when another card triggers them.");
    public static final Tooltip CLASH = new Tooltip("Clash",
            "<b>Clash</b> abilities activate on a minion whenever it attacks or is attacked by another minion.");
    public static final Tooltip LASTWORDS = new Tooltip("Last Words",
            "<b>Last Words</b> abilities activate whenever a card in play is destroyed.");
    public static final Tooltip STORM = new Tooltip("Storm",
            "Minions with <b>Storm</b> can immediately attack on the turn they're played.");
    public static final Tooltip RUSH = new Tooltip("Rush",
            "Minions with <b>Rush</b> can immediately attack enemy minions on the turn they're played.");
    public static final Tooltip WARD = new Tooltip("Ward",
            "If there is a minion with <b>Ward</b>, it must be attacked before targets without <b>Ward</b> can be attacked.");
    public static final Tooltip BANE = new Tooltip("Bane",
            "Minions with <b>Bane</b> automatically destroy the other minion after engaging in combat.");
    public static final Tooltip POISONOUS = new Tooltip("Poisonous",
            "Cards with <b>Poisonous</b> destroy any minion they damage.");
    public static final Tooltip COUNTDOWN = new Tooltip("Countdown",
            "At the start of the player's turn, cards in play with <b>Countdown</b> count down by one. When their <b>Countdown</b> goes to 0, the card is destroyed.");
    public static final Tooltip BLAST = new Tooltip("Blast(X)",
            "Deal X damage to a random enemy minion. If there are no enemy minions in play, deal X damage to the enemy leader.");
    public static final Tooltip AURA = new Tooltip("Aura",
            "<b>Aura</b> effects grant effects to other cards while the source of the aura is in play.");

    public final String name;
    public final String description;
    public String imagepath;
    public Tooltip[] references;

    public Tooltip(String name, String description, Tooltip... references) {
        this.name = name;
        this.description = description;
        this.references = references;
    }

    public Tooltip(String name, String description, String imagepath, Tooltip... references) {
        this(name, description, references);
        this.imagepath = imagepath;
    }
}
