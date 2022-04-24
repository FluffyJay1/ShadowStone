package server.card.effect;

public class EffectUntilTurnEndEnemy extends EffectUntilTurnEnd {
    //required for reflection
    public EffectUntilTurnEndEnemy() {

    }

    public EffectUntilTurnEndEnemy(String description) {
        super(description);
    }

    public EffectUntilTurnEndEnemy(String description, EffectStats stats) {
        super(description, stats);
    }
}
