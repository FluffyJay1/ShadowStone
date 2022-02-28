package server.resolver.meta;

import server.ServerBoard;
import server.card.effect.Effect;
import server.event.Event;
import server.resolver.Resolver;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.function.Predicate;

/**
 * Resolver to wrap a resolver to only execute if certain conditions about an
 * effect (most likely the effect that created it) are met, e.g. only execute if
 * the owner is still in play
 */
public class EffectPredicateResolver extends Resolver {
    Resolver resolverToWrap;
    Effect e;
    Predicate<Effect> predicate;

    public EffectPredicateResolver(Resolver resolver, Effect e, Predicate<Effect> predicate) {
        super(false);
        this.resolverToWrap = resolver;
        this.e = e;
        this.predicate = predicate;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        if (this.predicate.test(this.e)) {
            this.resolve(b, rq, el, this.resolverToWrap);
        }
    }
}
