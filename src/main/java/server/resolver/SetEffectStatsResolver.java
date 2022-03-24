package server.resolver;

import java.util.*;

import server.*;
import server.card.Card;
import server.card.effect.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class SetEffectStatsResolver extends Resolver {
    final List<? extends Effect> targets;
    final List<EffectStats> newStats;

    public SetEffectStatsResolver(List<? extends Effect> targets, List<EffectStats> newStats) {
        super(false);
        this.targets = targets;
        this.newStats = newStats;
    }

    public SetEffectStatsResolver(Effect target, EffectStats newStats) {
        this(List.of(target), List.of(newStats));
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        List<Card> markedForDeath = new ArrayList<>();
        b.processEvent(rq, el, new EventSetEffectStats(this.targets, this.newStats, markedForDeath));
        if (!markedForDeath.isEmpty()) {
            this.resolve(b, rq, el, new DestroyResolver(markedForDeath));
        }
    }
}
