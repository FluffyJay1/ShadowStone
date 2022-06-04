package server.card.effect;

import server.card.CardTrait;
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
    // set of stats that we'll ensure to keep non-negative in between applications
    private static final Set<Stat> NON_NEGATIVE_PER_STEP = new HashSet<>(List.of(Stat.SHIELD));

    public StatSet set = new StatSet(), change = new StatSet();

    // assume traits are always additive
    // we use treeset here to preserve ordering, so serialization order remains consistent
    public Set<CardTrait> traits = new TreeSet<>();

    public EffectStats() {

    }

    public EffectStats(int cost) {
        this();
        this.set.set(Stat.COST, cost);
    }

    public EffectStats(int cost, int attack, int magic, int health) {
        this(cost);
        this.set.set(Stat.ATTACK, attack);
        this.set.set(Stat.MAGIC, magic);
        this.set.set(Stat.HEALTH, health);
    }

    public void applyToStatSet(StatSet ss) {
        for (Map.Entry<Stat, Integer> entry : this.set.stats.entrySet()) {
            ss.set(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Stat, Integer> entry : this.change.stats.entrySet()) {
            ss.change(entry.getKey(), entry.getValue());
        }
        for (Stat i : NON_NEGATIVE_PER_STEP) {
            if (ss.get(i) < 0) {
                ss.set(i, 0);
            }
        }
    }

    public void resetStats() {
        this.set.reset();
        this.change.reset();
        this.traits.clear();
    }

    public boolean equalExcept(EffectStats other, Stat stat) {
        return this.set.equalExcept(other.set, stat) && this.change.equalsChangeExcept(other.change, stat) && this.traits.equals(other.traits);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EffectStats) {
            EffectStats other = (EffectStats) o;
            return this.set.equals(other.set) && this.change.equalsChange(other.change) && this.traits.equals(other.traits);
        }
        return false;
    }

    public void copy(EffectStats other) {
        this.set.copy(other.set);
        this.change.copy(other.change);
        this.traits.clear();
        this.traits.addAll(other.traits);
    }

    public EffectStats clone() {
        EffectStats es = new EffectStats();
        es.set = this.set.clone();
        es.change = this.change.clone();
        es.traits = new HashSet<>(this.traits);
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
        builder.append(this.traits.size()).append(" ");
        for (CardTrait trait : this.traits) {
            builder.append(trait.name()).append(" ");
        }
    }

    public static EffectStats fromString(StringTokenizer st) {
        EffectStats ret = new EffectStats();
        ret.set = StatSet.fromString(st);
        ret.change = StatSet.fromString(st);
        int numTraits = Integer.parseInt(st.nextToken());
        for (int i = 0; i < numTraits; i++) {
            ret.traits.add(CardTrait.valueOf(st.nextToken()));
        }
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
        private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);

        public int get(Stat index) {
            Integer result = this.stats.get(index);
            if (result == null) {
                return 0;
            }
            return result;
        }

        public boolean contains(Stat index) {
            return this.stats.containsKey(index);
        }

        public void set(Stat index, int stat) {
            this.stats.put(index, stat);
        }

        public void change(Stat index, int stat) {
            int old = this.get(index);
            this.set(index, old + stat);
        }

        public void reset(Stat index) {
            this.stats.remove(index);
        }

        public void reset() {
            this.stats.clear();
        }

        // lol
        public void makeNonNegative() {
            for (Stat stat : this.stats.keySet()) {
                if (this.get(stat) < 0) {
                    this.set(stat, 0);
                }
            }
        }

        public boolean equalExcept(StatSet other, Stat stat) {
            for (Stat i : Stat.values()) {
                if (!i.equals(stat) && (this.contains(i) != other.contains(i) || this.get(i) != other.get(i))) {
                    return false;
                }
            }
            return true;
        }

        // use and 0 is the same thing as unused
        public boolean equalsChange(StatSet other) {
            for (Stat i : Stat.values()) {
                if (this.get(i) != other.get(i)) {
                    return false;
                }
            }
            return true;
        }

        // use and 0 is the same thing as unused
        public boolean equalsChangeExcept(StatSet other, Stat stat) {
            for (Stat i : Stat.values()) {
                if (!i.equals(stat) && this.get(i) != other.get(i)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof StatSet) {
                StatSet other = (StatSet) o;
                return this.stats.equals(other.stats);
            }
            return false;
        }

        // if u don't wanna clone for some reason
        public void copy(StatSet other) {
            for (Stat i : Stat.values()) {
                if (other.contains(i)) {
                    this.set(i, other.get(i));
                } else {
                    this.reset(i);
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
            builder.append(this.stats.size()).append(" ");
            for (Map.Entry<Stat, Integer> entry : this.stats.entrySet()) {
                builder.append(entry.getKey().name()).append(" ").append(entry.getValue()).append(" ");
            }
        }

        public static StatSet fromString(StringTokenizer st) {
            StatSet ret = new StatSet();
            int numUsed = Integer.parseInt(st.nextToken());
            for (int i = 0; i < numUsed; i++) {
                Stat stat = Stat.valueOf(st.nextToken());
                int value = Integer.parseInt(st.nextToken());
                ret.set(stat, value);
            }
            return ret;
        }
    }

    public static class Builder {
        private final EffectStats built;

        Builder() {
            this.built = new EffectStats();
        }

        public Builder set(Stat stat, int value) {
            this.built.set.set(stat, value);
            return this;
        }

        public Builder change(Stat stat, int value) {
            this.built.change.set(stat, value);
            return this;
        }

        public Builder addTrait(CardTrait trait) {
            this.built.traits.add(trait);
            return this;
        }

        public EffectStats build() {
            return this.built;
        }
    }
}
