package server.card.effect;

public class EffectStatChange extends Effect {

    // required for reflection
    public EffectStatChange() { }

    public EffectStatChange(String description, int attack, int magic, int health) {
        super(description);
        this.effectStats.change.setStat(EffectStats.ATTACK, attack);
        this.effectStats.change.setStat(EffectStats.MAGIC, magic);
        this.effectStats.change.setStat(EffectStats.HEALTH, health);
    }
}
