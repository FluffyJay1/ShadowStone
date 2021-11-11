package server.resolver;

import java.util.*;

import server.*;
import server.card.effect.*;
import server.event.*;

public class SetEffectStatsResolver extends Resolver {
    Effect target;
    Effect newStats;

    public SetEffectStatsResolver(Effect target, Effect newStats) {
        super(false);
        this.target = target;
        this.newStats = newStats;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        EventSetEffectStats eses = b.processEvent(rl, el, new EventSetEffectStats(this.target, this.newStats));
        if (eses.markedForDeath) {
            this.resolve(b, rl, el, new DestroyResolver(List.of(target.owner)));
        }
    }
}
