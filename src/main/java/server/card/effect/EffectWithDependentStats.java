package server.card.effect;

/**
 * Some effects give stats that update dynamically based on the current state of
 * the board. The stats of these effects are therefore dependent on the state of
 * the board. Logic for keeping these stats up to date is handled in the
 * ServerBoard.
 */
public abstract class EffectWithDependentStats extends Effect {
    public EffectStats baselineStats; // stats to use when the calculation isn't active

    // required for reflection
    public EffectWithDependentStats() {
        this("");
    }

    public EffectWithDependentStats(String description) {
        this(description, true);
    }

    public EffectWithDependentStats(String description, boolean bonusStats) {
        this(description, bonusStats, new EffectStats());
    }
    public EffectWithDependentStats(String description, boolean bonusStats, EffectStats baselineStats) {
        this.baselineStats = baselineStats;
        this.effectStats.copy(baselineStats);
        this.description = description;
        this.bonusStats = bonusStats;
    }

    @Override
    public EffectWithDependentStats clone() throws CloneNotSupportedException {
        EffectWithDependentStats ret = (EffectWithDependentStats) super.clone();
        ret.baselineStats = this.baselineStats.clone();
        return ret;
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
