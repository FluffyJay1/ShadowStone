package server.resolver;

import server.Player;
import server.ServerBoard;
import server.card.Card;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.List;

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
            Card selection = SelectRandom.oneOfWith(this.p.getHand(), c -> c.finalStats.get(Stat.COST), Integer::min);
            if (selection != null) {
                this.resolve(b, rq, el, new DiscardResolver(selection));
            }
        }
    }
}
