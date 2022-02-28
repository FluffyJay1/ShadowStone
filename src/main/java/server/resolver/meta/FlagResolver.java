package server.resolver.meta;

import server.ServerBoard;
import server.card.Card;
import server.event.Event;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.resolver.Resolver;
import server.resolver.util.ResolverQueue;

import java.util.List;

/**
 * Resolver to wrap a resolver list with a flag eventgroup
 */
public class FlagResolver extends Resolver {
    final Card c;
    final ResolverQueue resolvers;
    public FlagResolver(Card c, ResolverQueue resolvers) {
        super(false);
        this.c = c;
        this.resolvers = resolvers;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        b.pushEventGroup(new EventGroup(EventGroupType.FLAG, List.of(c)));
        this.resolveQueue(b, rq, el, this.resolvers);
        b.popEventGroup();
    }
}
