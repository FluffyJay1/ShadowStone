package gamemode.dungeonrun.passive;

import java.util.List;
import java.util.stream.Stream;

import client.tooltip.Tooltip;
import gamemode.dungeonrun.Passive;
import server.ServerBoard;
import server.card.Card;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class TurnEndLowAttackBuff extends Passive {
    public static final String DESCRIPTION = "At the end of your turn, give all minions with more health than attack in your hand and board +1/+0/+0.";

    @Override
    public Tooltip getTooltip() {
        return new Tooltip("Grounds of the Summit Temple", DESCRIPTION, List::of);
    }

    @Override
    public List<Effect> getEffects() {
        return List.of(new EffectTurnEndLowAttackBuff());
    }
    
    public static class EffectTurnEndLowAttackBuff extends Effect {
        // required for reflection
        public EffectTurnEndLowAttackBuff() {
            super(DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onTurnEndAllied() {
            return new ResolverWithDescription(DESCRIPTION, new Resolver(true) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    List<Card> relevant = Stream.concat(owner.player.getPlayArea().stream(), owner.player.getHand().stream())
                            .filter(c -> c instanceof Minion && c.finalStats.get(Stat.ATTACK) < ((Minion) c).health)
                            .toList();
                    if (!relevant.isEmpty()) {
                        Effect buff = new Effect("+1/+0/+0 (from passive).", EffectStats.builder()
                                .change(Stat.ATTACK, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(relevant, buff));
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
