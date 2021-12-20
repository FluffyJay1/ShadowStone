package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

// deprecated resolver?
public class MillResolver extends Resolver {
    final Card c;

    public MillResolver(Card c) {
        super(false);
        this.c = c;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        // simple
        b.processEvent(rl, el, new EventMill(c));
        b.processEvent(rl, el, new EventDestroy(c));
    }
}
