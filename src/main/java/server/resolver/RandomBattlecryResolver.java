package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class RandomBattlecryResolver extends Resolver {
    final Card c;

    public RandomBattlecryResolver(Card c) {
        super(true);
        this.c = c;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        List<List<TargetingScheme<?>>> schemes = this.c.getBattlecryTargetingSchemes();
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
        this.resolveQueue(b, rq, el, this.c.battlecry(targets));
    }
}
