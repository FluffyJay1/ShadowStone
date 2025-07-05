package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class RandomUnleashResolver extends Resolver {
    final Card source;
    final Minion m;

    public RandomUnleashResolver(Card source, Minion m) {
        super(true);
        this.source = source;
        this.m = m;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        if (this.m.canBeUnleashed()) {
            List<List<TargetingScheme<?>>> schemes = this.m.getUnleashTargetingSchemes();
            List<List<TargetList<?>>> targets = new ArrayList<>();
            for (List<TargetingScheme<?>> schemesPerEffect : schemes) {
                List<TargetList<?>> targetsPerEffect = new ArrayList<>();
                for (TargetingScheme<?> schemeWithinEffect : schemesPerEffect) {
                    TargetList<?> targetsForScheme;
                    if (schemeWithinEffect.isApplicable(targetsPerEffect)) {
                        targetsForScheme = schemeWithinEffect.generateRandomTargets();
                    } else {
                        targetsForScheme = schemeWithinEffect.makeList();
                    }
                    targetsPerEffect.add(targetsForScheme);
                }
                targets.add(targetsPerEffect);
            }
            ResolverQueue unleash = this.m.unleash(targets);
            b.processEvent(rq, el, new EventUnleash(this.source, this.m));
            this.resolveQueue(b, rq, el, unleash);
        }
    }
}
