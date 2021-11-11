package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.card.unleashpower.*;
import server.event.*;

public class UnleashResolver extends Resolver {
    Card source;
    Minion m;
    String unleashTargets;

    public UnleashResolver(Card source, Minion m, String unleashTargets) {
        super(false);
        this.source = source;
        this.m = m;
        this.unleashTargets = unleashTargets;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        if (this.m.canBeUnleashed()) {
            // see PlayCardResolver for why we set the targets first
            Target.setListFromString(this.m.getUnleashTargets(), b, new StringTokenizer(this.unleashTargets));
            if (this.source instanceof UnleashPower) {
                Player p = ((UnleashPower) this.source).p;
                if (p.canUnleashCard(m)) {
                    b.processEvent(rl, el, new EventManaChange(p, -this.source.finalStatEffects.getStat(EffectStats.COST), false, true));
                    b.processEvent(rl, el, new EventUnleash(this.source, this.m));
                    this.resolveList(b, rl, el, p.unleashPower.onUnleashPre(this.m));
                    this.resolveList(b, rl, el, this.m.unleash());
                    this.resolveList(b, rl, el, p.unleashPower.onUnleashPost(this.m));
                } else {
                    return;
                }
            } else {
                b.processEvent(rl, el, new EventUnleash(this.source, this.m));
                this.resolveList(b, rl, el, this.m.unleash());
            }
        }
    }
}