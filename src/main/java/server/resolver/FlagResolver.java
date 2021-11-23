package server.resolver;

import server.Board;
import server.card.Card;
import server.event.Event;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;

import java.util.List;

/**
 * Resolver to wrap a resolver list with a flag eventgroup
 */
public class FlagResolver extends Resolver {
    Card c;
    List<Resolver> resolvers;
    public FlagResolver(Card c, List<Resolver> resolvers) {
        super(false);
        this.c = c;
        this.resolvers = resolvers;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        b.pushEventGroup(new EventGroup(EventGroupType.FLAG, List.of(c)));
        this.resolveList(b, rl, el, resolvers);
        b.popEventGroup();
    }
}
