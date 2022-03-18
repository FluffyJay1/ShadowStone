package server.card.effect;

import server.resolver.RemoveEffectResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

/**
 * For the common case of effects that last until the end of turn
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

    @Override
    public ResolverWithDescription onTurnEnd() {
        return new ResolverWithDescription("", new RemoveEffectResolver(List.of(this)));
    }

    @Override
    public ResolverWithDescription onTurnEndEnemy() {
        return new ResolverWithDescription("", new RemoveEffectResolver(List.of(this)));
    }
}
