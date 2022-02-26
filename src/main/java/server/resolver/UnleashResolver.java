package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.card.target.TargetList;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;

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
    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
        if (this.m.canBeUnleashed()) {
            this.m.setUnleashTargets(this.unleashTargets);
            if (this.source instanceof UnleashPower) {
                Player p = this.source.board.getPlayer(this.source.team);
                if (p.canUnleashCard(m)) {
                    b.processEvent(rl, el, new EventManaChange(p, -this.source.finalStatEffects.getStat(EffectStats.COST), false, true));
                    b.processEvent(rl, el, new EventUnleash(this.source, this.m));
                    p.getUnleashPower().ifPresent(up -> this.resolveList(b, rl, el, up.onUnleashPre(this.m)));
                    b.pushEventGroup(new EventGroup(EventGroupType.UNLEASH, List.of(this.m)));
                    this.resolveList(b, rl, el, this.m.unleash());
                    b.popEventGroup();
                    p.getUnleashPower().ifPresent(up -> this.resolveList(b, rl, el, up.onUnleashPost(this.m)));
                }
            } else {
                b.processEvent(rl, el, new EventUnleash(this.source, this.m));
                b.pushEventGroup(new EventGroup(EventGroupType.UNLEASH, List.of(this.m)));
                this.resolveList(b, rl, el, this.m.unleash());
                b.popEventGroup();
            }
        }
    }
}
