package server.card.effect;

import utils.StringBuildable;

import java.util.*;

/**
 * Class to manage the setting of stats granted by effects. Every effect has one
 * of these, and a Card iterates through each effect to look at these to
 * determine what final stats the card should have.
 *
 * Boils down to 2 sets of stats: what stats this effect "sets" (like an effect
 * that sets the attack to 2), and what stats this effect "changes" or adds to
 * (like an effect that buffs health by 2)
 */
public class EffectStats implements Cloneable, StringBuildable {
    public static final int NUM_STATS = 15;
    // what's an enum
    public static final int COST = 0, ATTACK = 1, MAGIC = 2, HEALTH = 3, ATTACKS_PER_TURN = 4, STORM = 5, RUSH = 6,
            WARD = 7, BANE = 8, POISONOUS = 9, COUNTDOWN = 10, SPELLBOOSTABLE = 11, LIFESTEAL = 12,
            STEALTH = 13, SHIELD = 14;
    // set of stats that we'll ensure to keep non-negative in between applications
    private static final Set<Integer> NON_NEGATIVE_PER_STEP = new HashSet<>(List.of(SHIELD));
    private static final String USED_INDICATOR = "Y";
    private static final String UNUSED_INDICATOR = "N";

    public StatSet set = new StatSet(), change = new StatSet();

    public EffectStats() {

    }

    public EffectStats(int cost) {
        this();
        this.set.setStat(COST, cost);
    }

    public EffectStats(int cost, int attack, int magic, int health) {
        this(cost);
        this.set.setStat(ATTACK, attack);
        this.set.setStat(MAGIC, magic);
        this.set.setStat(HEALTH, health);
    }

    public void applyToStatSet(StatSet ss) {
        for (int i = 0; i < this.set.stats.length; i++) {
            if (this.set.use[i]) {
                ss.setStat(i, this.set.stats[i]);
            }
        }
        for (int i = 0; i < this.change.stats.length; i++) {
            if (this.change.use[i]) {
                ss.changeStat(i, this.change.stats[i]);
            }
        }
        for (int i : NON_NEGATIVE_PER_STEP) {
            if (ss.getStat(i) < 0) {
                ss.setStat(i, 0);
            }
        }
    }

    public void resetStats() {
        this.set.reset();
        this.change.reset();
    }

    public boolean equalExcept(EffectStats other, int stat) {
        return this.set.equalExcept(other.set, stat) && this.change.equalsChangeExcept(other.change, stat);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EffectStats) {
            EffectStats other = (EffectStats) o;
            return this.set.equals(other.set) && this.change.equalsChange(other.change);
        }
        return false;
    }

    public void copy(EffectStats other) {
        this.set.copy(other.set);
        this.change.copy(other.change);
    }

    public EffectStats clone() {
        EffectStats es = new EffectStats();
        es.set = this.set.clone();
        es.change = this.change.clone();
        return es;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.appendStringToBuilder(builder);
        return builder.toString();
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        this.set.appendStringToBuilder(builder);
        this.change.appendStringToBuilder(builder);
    }

    public static EffectStats fromString(StringTokenizer st) {
        EffectStats ret = new EffectStats();
        ret.set = StatSet.fromString(st);
        ret.change = StatSet.fromString(st);
        return ret;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Helper class to assign each stat (cost, attack, etc.) a value. Boolean
     * stats (poison, rush, etc.) are denoted as 1 for true and 0 for false.
     *
     * Why is this an inner class? Who cares?
     */
    public static class StatSet implements Cloneable, StringBuildable {
        private int numUsed = 0;
        private final int[] stats = new int[NUM_STATS];
        private final boolean[] use = new boolean[NUM_STATS];

        public int getStat(int index) {
            if (!this.use[index]) {
                return 0;
            }
            return this.stats[index]; // lol
        }

        public boolean getUse(int index) {
            return this.use[index];
        }

        public void setStat(int index, int stat) {
            this.stats[index] = stat;
            if (!this.use[index]) {
                this.numUsed++;
            }
            this.use[index] = true;
        }

        public void changeStat(int index, int stat) {
            this.stats[index] += stat;
            if (!this.use[index]) {
                this.numUsed++;
            }
            this.use[index] = true;
        }

        public void resetStat(int index) {
            this.stats[index] = 0;
            if (this.use[index]) {
                this.numUsed--;
            }
            this.use[index] = false;
        }

        public void reset() {
            for (int i = 0; i < this.stats.length; i++) {
                this.resetStat(i);
            }
        }

        // lol
        public void makeNonNegative() {
            for (int i = 0; i < this.stats.length; i++) {
                if (this.getStat(i) < 0) {
                    this.setStat(i, 0);
                }
            }
        }

        public boolean equalExcept(StatSet other, int stat) {
            for (int i = 0; i < this.stats.length; i++) {
                if (i != stat && (this.getUse(i) != other.getUse(i) || this.getStat(i) != other.getStat(i))) {
                    return false;
                }
            }
            return true;
        }

        // use and 0 is the same thing as unused
        public boolean equalsChange(StatSet other) {
            for (int i = 0; i < this.stats.length; i++) {
                if (this.getStat(i) != other.getStat(i)) {
                    return false;
                }
            }
            return true;
        }

        // use and 0 is the same thing as unused
        public boolean equalsChangeExcept(StatSet other, int stat) {
            for (int i = 0; i < this.stats.length; i++) {
                if (i != stat && this.getStat(i) != other.getStat(i)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof StatSet) {
                StatSet other = (StatSet) o;
                return Arrays.equals(this.stats, other.stats) && Arrays.equals(this.use, other.use);
            }
            return false;
        }

        // if u don't wanna clone for some reason
        public void copy(StatSet other) {
            for (int i = 0; i < this.stats.length; i++) {
                if (other.use[i]) {
                    this.setStat(i, other.stats[i]);
                } else {
                    this.resetStat(i);
                }
            }
        }

        @Override
        public StatSet clone() {
            StatSet ret = new StatSet();
            ret.copy(this);
            return ret;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            this.appendStringToBuilder(builder);
            return builder.toString();
        }

        @Override
        public void appendStringToBuilder(StringBuilder builder) {
            if (this.numUsed == 0) {
                builder.append(UNUSED_INDICATOR).append(" ");
            } else {
                builder.append(USED_INDICATOR).append(" ");
                for (int i = 0; i < NUM_STATS; i++) {
                    builder.append(use[i] ? "T" : "F").append(" ").append(stats[i]).append(" ");
                }
            }
        }

        public static StatSet fromString(StringTokenizer st) {
            StatSet ret = new StatSet();
            String indicator = st.nextToken();
            if (indicator.equals(UNUSED_INDICATOR)) {
                return ret;
            }
            for (int i = 0; i < NUM_STATS; i++) {
                boolean use = st.nextToken().equals("T");
                int stat = Integer.parseInt(st.nextToken());
                if (use) {
                    ret.setStat(i, stat);
                }
            }
            return ret;
        }
    }

    public static class Builder {
        private EffectStats built;

        Builder() {
            this.built = new EffectStats();
        }

        public Builder set(int stat, int value) {
            this.built.set.setStat(stat, value);
            return this;
        }

        public Builder change(int stat, int value) {
            this.built.change.setStat(stat, value);
            return this;
        }

        public EffectStats build() {
            return this.built;
        }
    }
}
