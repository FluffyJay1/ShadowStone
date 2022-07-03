package server.resolver;

import server.ServerBoard;
import server.card.Card;
import server.event.Event;
import server.event.EventDestroy;
import server.event.EventMuteEffect;
import server.resolver.util.ResolverQueue;

import java.util.ArrayList;
import java.util.List;

public class MuteResolver extends Resolver {
    List<? extends Card> c;
    public List<Card> markedForDeath;
    boolean resolveDestroy;

    public MuteResolver(List<? extends Card> c, boolean resolveDestroy) {
        super(false);
        this.c = c;
        this.resolveDestroy = resolveDestroy;
        this.markedForDeath = new ArrayList<>(c.size());
    }

    public MuteResolver(Card c, boolean resolveDestroy) {
        this(List.of(c), resolveDestroy);
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        b.processEvent(rq, el, new EventMuteEffect(this.c, true, this.markedForDeath));
        if (this.resolveDestroy) {
            this.resolve(b, rq, el, new DestroyResolver(this.markedForDeath, EventDestroy.Cause.NATURAL));
        }
    }
}
