package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.card.target.TargetList;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class UnleashResolver extends Resolver {
    final Card source;
    final Minion m;
    final List<List<TargetList<?>>> unleashTargets;

    public UnleashResolver(Card source, Minion m, List<List<TargetList<?>>> unleashTargets) {
        super(false);
        this.source = source;
        this.m = m;
        this.unleashTargets = unleashTargets;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        if (this.m.canBeUnleashed()) {
            ResolverQueue unleash = this.m.unleash(this.unleashTargets);
            if (this.source instanceof UnleashPower) {
                Player p = this.source.board.getPlayer(this.source.team);
                if (p.canUnleashCard(m)) {
                    b.processEvent(rq, el, new EventManaChange(p, -this.source.finalStats.get(Stat.COST), true, false));
                    b.processEvent(rq, el, new EventUnleash(this.source, this.m));
                    p.getUnleashPower().ifPresent(up -> this.resolveQueue(b, rq, el, up.onUnleashPre(this.m)));
                    this.resolveQueue(b, rq, el, unleash);
                    p.getUnleashPower().ifPresent(up -> this.resolveQueue(b, rq, el, up.onUnleashPost(this.m)));
                }
            } else {
                b.processEvent(rq, el, new EventUnleash(this.source, this.m));
                this.resolveQueue(b, rq, el, unleash);
            }
        }
    }
}
