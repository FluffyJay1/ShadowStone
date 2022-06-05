package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.Stat;
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
        List<Card> cardsToDestroy = new ArrayList<>(this.cards.size());
        List<Card> cardsToBanish = new ArrayList<>(this.cards.size());
        for (Card c : this.cards) {
            if (c.finalStats.get(Stat.BANISH_ON_DESTROY) > 0 && c.isInPlay()) {
                cardsToBanish.add(c);
            } else {
                cardsToDestroy.add(c);
            }
        }
        if (!cardsToBanish.isEmpty()) {
            this.resolve(b, rq, el, new BanishResolver(cardsToBanish));
        }
        if (!cardsToDestroy.isEmpty()) {
            EventDestroy destroy = new EventDestroy(cardsToDestroy);
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
