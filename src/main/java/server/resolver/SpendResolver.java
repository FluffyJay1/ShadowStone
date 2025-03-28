package server.resolver;

import server.Player;
import server.ServerBoard;
import server.card.effect.Effect;
import server.event.Event;
import server.event.EventSpend;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class SpendResolver extends Resolver {
    Effect source;
    int amount;
    Resolver onSuccess;
    boolean wasSuccessful;

    public SpendResolver(Effect source, int amount, Resolver onSuccess) {
        super(false);
        this.source = source;
        this.amount = amount;
        this.onSuccess = onSuccess;
        this.wasSuccessful = false;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        Player p = b.getPlayer(this.source.owner.team);
        if (p.mana >= this.amount) {
            this.wasSuccessful = true;
            b.processEvent(rq, el, new EventSpend(this.source, this.amount));
            if (this.onSuccess != null) {
                this.resolve(b, rq, el, this.onSuccess);
            }
        }
    }

    public boolean wasSuccessful() {
        return this.wasSuccessful;
    }
}
