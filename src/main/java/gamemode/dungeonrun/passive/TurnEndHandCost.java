package gamemode.dungeonrun.passive;

import java.util.List;

import client.tooltip.Tooltip;
import gamemode.dungeonrun.Passive;
import server.ServerBoard;
import server.card.Card;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

public class TurnEndHandCost extends Passive {
    public static final String DESCRIPTION = "At the end of your turn, subtract 2 from the cost of a random card in your hand.";

    @Override
    public Tooltip getTooltip() {
        return new Tooltip("Hand Cost Reducer", DESCRIPTION, List::of);
    }

    @Override
    public List<Effect> getEffects() {
        return List.of(new EffectTurnEndHandCost());
    }
    
    public static class EffectTurnEndHandCost extends Effect {
        // required for reflection
        public EffectTurnEndHandCost() {
            super(DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onTurnEndAllied() {
            return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    // prioritize cards that cost more than 0
                    Card choice = SelectRandom.oneOfWith(owner.player.getHand(), c -> c.finalStats.get(Stat.COST) > 0 ? 1 : 0, Integer::max);
                    if (choice != null) {
                        Effect buff = new Effect("-2 cost (from passive).", EffectStats.builder()
                                .change(Stat.COST, -2)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(choice, buff));
                    }
                }
            });
        }

        @Override
        public double getPresenceValue(int refs) {
            return 2;
        }
    }
}
