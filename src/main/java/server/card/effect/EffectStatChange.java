package server.card.effect;

public class EffectStatChange extends Effect {

    public EffectStatChange(String description) {
        super(description);
    }

    public EffectStatChange(String description, int attack, int magic, int health) {
        super(description);
        this.change.setStat(EffectStats.ATTACK, attack);
        this.change.setStat(EffectStats.MAGIC, magic);
        this.change.setStat(EffectStats.HEALTH, health);
    }
}
