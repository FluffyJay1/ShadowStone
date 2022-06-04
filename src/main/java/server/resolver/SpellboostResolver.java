package server.resolver;

import server.ServerBoard;
import server.card.Card;
import server.card.effect.Stat;
import server.event.Event;
import server.event.EventSpellboost;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

public class SpellboostResolver extends Resolver {
    List<Card> cards;

    public SpellboostResolver(List<Card> cards) {
        super(false);
        this.cards = cards;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        List<Card> spellboostable = this.cards.stream()
                .filter(c -> c.finalStats.get(Stat.SPELLBOOSTABLE) > 0)
                .collect(Collectors.toList());
        if (!spellboostable.isEmpty()) {
            b.processEvent(rq, el, new EventSpellboost(spellboostable));
        }
    }
}
