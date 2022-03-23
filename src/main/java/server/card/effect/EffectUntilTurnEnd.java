package server.card.effect;

import server.resolver.RemoveEffectResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

/**
 * For the common case of effects that last until the end of turn
 * When the Card adds one of these effects, it keeps track in the
 * ServerBoard, then the TurnEndResolver checks these and removes them
 */
public class EffectUntilTurnEnd extends Effect {
    //required for reflection
    public EffectUntilTurnEnd() {

    }

    public EffectUntilTurnEnd(String description) {
        super(description);
    }

    public EffectUntilTurnEnd(String description, EffectStats stats) {
        super(description, stats);
    }
}
