package server.resolver;

import server.ServerBoard;
import server.card.Card;
import server.card.CardStatus;
import server.event.Event;
import server.event.EventDiscard;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class DiscardResolver extends Resolver {
    List<? extends Card> cards;

    public DiscardResolver(List<? extends Card> cards) {
        super(false);
        this.cards = cards;
    }

    public DiscardResolver(Card card) {
        this(List.of(card));
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        List<Card> cardsInHand = this.cards.stream()
                .filter(c -> c.status.equals(CardStatus.HAND))
                .collect(Collectors.toList());
        if (!cardsInHand.isEmpty()) {
            EventDiscard discard = new EventDiscard(cardsInHand);
            b.processEvent(rq, el, discard);
        }
    }
}
