package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class BanishResolver extends Resolver {
    public final List<? extends Card> c;

    public BanishResolver(List<? extends Card> c) {
        super(false);
        this.c = c;
        this.essential = true;
    }

    public BanishResolver(Card c) {
        this(List.of(c));
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        b.processEvent(rq, el, new EventBanish(this.c));
    }
}
