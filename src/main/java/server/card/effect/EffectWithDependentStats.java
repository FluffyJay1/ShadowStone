package server.card.effect;

/**
 * Some effects give stats that update dynamically based on the current state of
 * the board. The stats of these effects are therefore dependent on the state of
 * the board. Logic for keeping these stats up to date is handled in the
 * ServerBoard.
 */
public abstract class EffectWithDependentStats extends Effect {
    public boolean awaitingUpdate; // whether the server has issued an update resolver already for this effect
    public EffectStats lastCheckedExpectedStats;

    // required for reflection
    public EffectWithDependentStats() {
        this.awaitingUpdate = false;
        this.lastCheckedExpectedStats = new EffectStats();
    }

    public EffectWithDependentStats(String description) {
        this();
        this.description = description;
    }

    public EffectWithDependentStats(String description, boolean bonusStats) {
        this(description);
        this.bonusStats = bonusStats;
    }

    /**
     * Calculate what the stats should be given the current state of the board
     * @return The expected stats
     */
    public abstract EffectStats calculateStats();

    /**
     * Sometimes these stats aren't always active, like some minion attack buffs
     * should only be active when they actually hit the board.
     * @return Whether the calculated stats should be active or not
     */
    public abstract boolean isActive();
}
