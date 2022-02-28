package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class PutCardResolver extends Resolver {

    private final int team;
    private final List<Card> c;
    private final CardStatus status;
    private final List<Integer> pos;
    public final List<Card> destroyed;

    public PutCardResolver(List<Card> c, CardStatus status, int team, List<Integer> pos) {
        super(false);
        this.c = c;
        this.status = status;
        this.team = team;
        this.pos = pos;
        this.destroyed = new LinkedList<>();
    }

    public PutCardResolver(Card c, CardStatus status, int team, int pos) {
        this(List.of(c), status, team, List.of(pos));
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        b.processEvent(rq, el, new EventPutCard(this.c, this.status, this.team, this.pos, this.destroyed));
        this.resolve(b, rq, el, new DestroyResolver(this.destroyed));
    }

}
