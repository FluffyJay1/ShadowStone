package server.card.effect;

public class EffectUntilTurnEndAllied extends EffectUntilTurnEnd {
    //required for reflection
    public EffectUntilTurnEndAllied() {

    }

    public EffectUntilTurnEndAllied(String description) {
        super(description);
    }

    public EffectUntilTurnEndAllied(String description, EffectStats stats) {
        super(description, stats);
    }
}
