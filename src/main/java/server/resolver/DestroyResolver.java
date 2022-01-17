package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

public class DestroyResolver extends Resolver {
    final List<Card> cards;

    public DestroyResolver(List<Card> cards) {
        super(false);
        this.cards = cards;
    }

    public DestroyResolver(Card card) {
        this(List.of(card));
    }

    @Override
    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
        if (!this.cards.isEmpty()) {
            EventDestroy destroy = new EventDestroy(this.cards);
            b.processEvent(rl, el, destroy);
            for (BoardObject bo : destroy.cardsLeavingPlay()) {
                if (bo instanceof Leader) {
                    b.processEvent(rl, el, new EventGameEnd(bo.board, bo.team * -1));
                }
                rl.add(new LastWordsResolver(bo));
            }
        }
    }
}
