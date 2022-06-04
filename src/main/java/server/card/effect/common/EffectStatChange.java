package server.card.effect.common;

import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

public class EffectStatChange extends Effect {

    // required for reflection
    public EffectStatChange() { }

    public EffectStatChange(String description, int attack, int magic, int health) {
        super(description);
        this.effectStats.change.set(Stat.ATTACK, attack);
        this.effectStats.change.set(Stat.MAGIC, magic);
        this.effectStats.change.set(Stat.HEALTH, health);
    }
}
