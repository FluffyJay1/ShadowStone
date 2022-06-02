package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class PutCardResolver extends Resolver {

    private final int team;
    private final List<? extends Card> c;
    private final CardStatus status;
    private final List<Integer> pos;
    private final boolean resolveDestroy;
    public final List<Card> destroyed;

    public PutCardResolver(List<? extends Card> c, CardStatus status, int team, List<Integer> pos, boolean resolveDestroy) {
        super(false);
        this.c = c;
        this.status = status;
        this.team = team;
        this.pos = pos;
        this.resolveDestroy = resolveDestroy;
        this.destroyed = new LinkedList<>();
    }

    public PutCardResolver(Card c, CardStatus status, int team, int pos, boolean resolveDestroy) {
        this(List.of(c), status, team, List.of(pos), resolveDestroy);
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        b.processEvent(rq, el, new EventPutCard(this.c, this.status, this.team, this.pos, false, this.destroyed));
        if (this.resolveDestroy) {
            this.resolve(b, rq, el, new DestroyResolver(this.destroyed));
        }
    }

}
