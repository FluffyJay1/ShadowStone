package server.card.effect.common;

import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.event.EventUnleash;
import server.resolver.RemoveEffectResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class EffectUnleashPowerOneTimeCost extends Effect {
    // required for reflection
    public EffectUnleashPowerOneTimeCost() {

    }

    public EffectUnleashPowerOneTimeCost(String sourceString, int cost) {
        super("Cost set to " + cost + " for 1 use (from " + sourceString + ").", EffectStats.builder()
                .set(Stat.COST, cost)
                .build());
    }

    @Override
    public ResolverWithDescription onListenEventWhileInPlay(Event event) {
        if (event instanceof EventUnleash) {
            EventUnleash eu = (EventUnleash) event;
            if (eu.source == this.owner) {
                return new ResolverWithDescription(null, new RemoveEffectResolver(List.of(this)));
            }
        }
        return null;
    }
}
