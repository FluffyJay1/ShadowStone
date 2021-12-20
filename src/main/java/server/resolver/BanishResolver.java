package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

public class BanishResolver extends Resolver {
    public final List<Card> c;

    public BanishResolver(List<Card> c) {
        super(false);
        this.c = c;
    }

    public BanishResolver(Card c) {
        this(List.of(c));
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        b.processEvent(rl, el, new EventBanish(this.c));
    }
}
