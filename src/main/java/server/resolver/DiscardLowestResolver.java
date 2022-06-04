package server.resolver;

import client.Game;
import server.Player;
import server.ServerBoard;
import server.card.Card;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.util.ResolverQueue;

import java.util.List;
import java.util.stream.Collectors;

// for the common case of randomly discarding the lowest cost card in a player's hand (once)
public class DiscardLowestResolver extends Resolver {
    Player p;
    int times;
    public DiscardLowestResolver(Player p, int times) {
        super(true);
        this.p = p;
        this.times = times;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        for (int i = 0; i < this.times; i++) {
            this.p.getHand().stream()
                    .map(c -> c.finalStats.get(Stat.COST))
                    .min(Integer::compareTo)
                    .ifPresent(cost -> {
                        List<Card> lowest = p.getHand().stream()
                                .filter(c -> c.finalStats.get(Stat.COST) == cost)
                                .collect(Collectors.toList());
                        Card selection = Game.selectRandom(lowest);
                        this.resolve(b, rq, el, new DiscardResolver(selection));
                    });
        }
    }
}
