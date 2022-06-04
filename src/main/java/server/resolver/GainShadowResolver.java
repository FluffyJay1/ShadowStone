package server.resolver;

import server.Player;
import server.ServerBoard;
import server.event.Event;
import server.event.EventGainShadow;
import server.resolver.util.ResolverQueue;

import java.util.List;

// this class looks useless but this layer of indirection might be useful
public class GainShadowResolver extends Resolver {
    Player p;
    int amount;

    public GainShadowResolver(Player p, int amount) {
        super(false);
        this.p = p;
        this.amount = amount;
    }
    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        b.processEvent(rq, el, new EventGainShadow(this.p, this.amount));
    }
}
