package server.resolver;

import server.Player;
import server.ServerBoard;
import server.card.effect.Effect;
import server.event.Event;
import server.event.EventNecromancy;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class NecromancyResolver extends Resolver {
    Effect source;
    int shadows;
    Resolver onSuccess;
    boolean wasSuccessful;

    public NecromancyResolver(Effect source, int shadows, Resolver onSuccess) {
        super(false);
        this.source = source;
        this.shadows = shadows;
        this.onSuccess = onSuccess;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        Player p = b.getPlayer(this.source.owner.team);
        if (p.shadows >= this.shadows) {
            this.wasSuccessful = true;
            b.processEvent(rq, el, new EventNecromancy(this.source, this.shadows));
            this.resolve(b, rq, el, this.onSuccess);
        }
    }

    public boolean wasSuccessful() {
        return this.wasSuccessful;
    }
}
