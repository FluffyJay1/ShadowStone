package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

public class DestroyResolver extends Resolver {
    List<Card> cards;

    public DestroyResolver(List<Card> cards) {
        super(false);
        this.cards = cards;
    }

    public DestroyResolver(Card card) {
        this(List.of(card));
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        if (!this.cards.isEmpty()) {
            EventDestroy destroy = new EventDestroy(this.cards);
            b.processEvent(rl, el, destroy);
            for (Card c : destroy.cardsLeavingPlay()) {
                if (c instanceof Leader) {
                    b.processEvent(rl, el, new EventGameEnd(c.board, c.team * -1));
                }
                BoardObject bo = (BoardObject) c;
                List<Resolver> lastwords = bo.lastWords();
                if (!lastwords.isEmpty()) {
                    b.processEvent(rl, el, new EventLastWords(bo));
                    rl.addAll(lastwords);
                }
            }
        }
    }
}
