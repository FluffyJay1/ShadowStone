package server.resolver;

import java.util.*;

import client.ui.game.visualboardanimation.eventanimation.destroy.EventAnimationDestroy;
import server.*;
import server.card.*;
import server.card.effect.Stat;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class DestroyResolver extends Resolver {
    final List<? extends Card> cards;
    final EventDestroy.Cause cause;
    final String animationString;

    public DestroyResolver(List<? extends Card> cards, EventDestroy.Cause cause, EventAnimationDestroy animation) {
        super(false);
        this.cards = cards;
        this.cause = cause;
        this.essential = true;
        this.animationString = EventAnimationDestroy.stringOrNull(animation);
    }
    public DestroyResolver(List<? extends Card> cards, EventDestroy.Cause cause) {
        this(cards, cause, null);
    }

    public DestroyResolver(List<? extends Card> cards) {
        this(cards, EventDestroy.Cause.EFFECT);
    }

    public DestroyResolver(List<? extends Card> cards, EventAnimationDestroy animation) {
        this(cards, EventDestroy.Cause.EFFECT, animation);
    }

    public DestroyResolver(Card card, EventAnimationDestroy animation) {
        this(List.of(card), animation);
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
            EventDestroy destroy = new EventDestroy(cardsToDestroy, this.cause, this.animationString);
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
