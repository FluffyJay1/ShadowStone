package server.resolver;

import server.ServerBoard;
import server.card.BoardObject;
import server.event.Event;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.resolver.util.ResolverQueue;

import java.util.List;

/**
 * This class exists outside of the DestroyResolver because we want
 * last words resolution phase to happen after any top level resolvers,
 * and we also want to wrap the last words resolution with an eventgroup
 */
public class LastWordsResolver extends Resolver {
    final BoardObject bo;
    public LastWordsResolver(BoardObject bo) {
        super(false);
        this.bo = bo;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        b.pushEventGroup(new EventGroup(EventGroupType.LASTWORDS, List.of(this.bo)));
        this.resolveQueue(b, rq, el, this.bo.lastWords());
        b.popEventGroup();
    }
}
