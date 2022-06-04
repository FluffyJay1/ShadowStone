package server.resolver;

import server.Player;
import server.ServerBoard;
import server.event.Event;
import server.event.EventManaChange;
import server.resolver.util.ResolverQueue;

import java.util.List;

// this class looks useless but this layer of indirection might be useful
public class ManaChangeResolver extends Resolver {
    Player p;
    int amount;
    boolean changeCurrent, changeMax;
    public ManaChangeResolver(Player p, int amount, boolean changeCurrent, boolean changeMax) {
        super(false);
        this.p = p;
        this.amount = amount;
        this.changeCurrent = changeCurrent;
        this.changeMax = changeMax;
    }
    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        b.processEvent(rq, el, new EventManaChange(this.p, this.amount, this.changeCurrent, this.changeMax));
    }
}
