package gamemode.dungeonrun.passive;

import java.util.List;

import client.tooltip.Tooltip;
import gamemode.dungeonrun.Passive;
import server.ServerBoard;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.RemoveEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class GameStartHandCost extends Passive {
    public static final String DESCRIPTION = "At the start of your first turn, subtract 3 from the cost of all cards in your hand.";

    @Override
    public Tooltip getTooltip() {
        return new Tooltip("Small Loan", DESCRIPTION, List::of);
    }

    @Override
    public List<Effect> getEffects() {
        return List.of(new EffectGameStartHandCost());
    }
    
    public static class EffectGameStartHandCost extends Effect {
        // required for reflection
        public EffectGameStartHandCost() {
            super(DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onTurnStartAllied() {
            return new ResolverWithDescription(DESCRIPTION, new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    Effect buff = new Effect("-3 cost (from passive).", EffectStats.builder()
                            .change(Stat.COST, -3)
                            .build());
                    this.resolve(b, rq, el, new AddEffectResolver(owner.player.getHand(), buff));
                    this.resolve(b, rq, el, new RemoveEffectResolver(List.of(EffectGameStartHandCost.this)));
                }
            });
        }

        @Override
        public double getPresenceValue(int refs) {
            return 2;
        }
    }
}
