package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class DestroyResolver extends Resolver {
    final List<? extends Card> cards;

    public DestroyResolver(List<? extends Card> cards) {
        super(false);
        this.cards = cards;
        this.essential = true;
    }

    public DestroyResolver(Card card) {
        this(List.of(card));
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        if (!this.cards.isEmpty()) {
            EventDestroy destroy = new EventDestroy(this.cards);
            b.processEvent(rq, el, destroy);
            for (BoardObject bo : destroy.cardsLeavingPlay()) {
                if (bo instanceof Leader) {
                    b.processEvent(rq, el, new EventGameEnd(bo.board, bo.team * -1));
                }
                rq.addAll(bo.lastWords());
            }
        }
    }
}
