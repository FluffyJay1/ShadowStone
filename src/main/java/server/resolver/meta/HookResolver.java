package server.resolver.meta;

import server.ServerBoard;
import server.card.Card;
import server.card.effect.Effect;
import server.event.Event;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.resolver.Resolver;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.function.Predicate;

/**
 * Resolver for an effect hook
 * Wrap a resolver + description combo into an actual resolver
 * only execute if certain conditions about an effect (most likely the effect
 * that created it) are met, e.g. only execute if the owner is still in play
 */
public class HookResolver extends Resolver {
    final EventGroupType etype;
    final List<Card> c;
    final ResolverWithDescription rwd;
    final Predicate<Effect> predicate;
    final Effect e;
    public HookResolver(EventGroupType etype, List<Card> cards, ResolverWithDescription rwd, Effect e, Predicate<Effect> predicate) {
        super(false);
        this.etype = etype;
        this.c = cards;
        this.rwd = rwd;
        this.e = e;
        this.predicate = predicate;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        if (this.predicate.test(this.e)) {
            if (this.rwd.description != null) {
                b.pushEventGroup(new EventGroup(this.etype, this.c, this.rwd.description));
            }
            this.resolve(b, rq, el, this.rwd.r);
            if (this.rwd.description != null) {
                b.popEventGroup();
            }
        }
    }
}
