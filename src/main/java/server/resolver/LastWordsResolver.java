package server.resolver;

import server.Board;
import server.card.BoardObject;
import server.card.effect.Effect;
import server.event.Event;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;

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
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        b.pushEventGroup(new EventGroup(EventGroupType.LASTWORDS, List.of(bo)));
        this.resolveList(b, rl, el, bo.getResolvers(Effect::lastWords));
        b.popEventGroup();
    }
}
