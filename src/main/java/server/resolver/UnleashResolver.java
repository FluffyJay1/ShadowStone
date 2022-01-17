package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.card.unleashpower.*;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;

public class UnleashResolver extends Resolver {
    final Card source;
    final Minion m;
    final String unleashTargets;

    public UnleashResolver(Card source, Minion m, String unleashTargets) {
        super(false);
        this.source = source;
        this.m = m;
        this.unleashTargets = unleashTargets;
    }

    @Override
    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
        if (this.m.canBeUnleashed()) {
            // see PlayCardResolver for why we set the targets first
            Target.setListFromString(this.m.getUnleashTargets(), b, new StringTokenizer(this.unleashTargets));
            if (this.source instanceof UnleashPower) {
                Player p = this.source.board.getPlayer(this.source.team);
                if (p.canUnleashCard(m)) {
                    b.processEvent(rl, el, new EventManaChange(p, -this.source.finalStatEffects.getStat(EffectStats.COST), false, true));
                    b.processEvent(rl, el, new EventUnleash(this.source, this.m));
                    this.resolveList(b, rl, el, p.getUnleashPower().onUnleashPre(this.m));
                    b.pushEventGroup(new EventGroup(EventGroupType.UNLEASH, List.of(this.m)));
                    this.resolveList(b, rl, el, this.m.getResolvers(Effect::unleash));
                    b.popEventGroup();
                    this.resolveList(b, rl, el, p.getUnleashPower().onUnleashPost(this.m));
                }
            } else {
                b.processEvent(rl, el, new EventUnleash(this.source, this.m));
                b.pushEventGroup(new EventGroup(EventGroupType.UNLEASH, List.of(this.m)));
                this.resolveList(b, rl, el, this.m.getResolvers(Effect::unleash));
                b.popEventGroup();
            }
        }
    }
}
