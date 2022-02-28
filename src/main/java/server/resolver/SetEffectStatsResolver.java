package server.resolver;

import java.util.*;

import server.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class SetEffectStatsResolver extends Resolver {
    final Effect target;
    final EffectStats newStats;

    public SetEffectStatsResolver(Effect target, EffectStats newStats) {
        super(false);
        this.target = target;
        this.newStats = newStats;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        EventSetEffectStats eses = b.processEvent(rq, el, new EventSetEffectStats(this.target, this.newStats));
        if (eses.markedForDeath) {
            this.resolve(b, rq, el, new DestroyResolver(List.of(target.owner)));
        }
    }
}
