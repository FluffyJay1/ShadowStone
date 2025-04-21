package gamemode.dungeonrun.passive;

import java.util.List;

import client.tooltip.Tooltip;
import gamemode.dungeonrun.Passive;
import server.ServerBoard;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.event.Event;
import server.event.EventMinionAttack;
import server.resolver.AddEffectResolver;
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class OnAttackStats extends Passive {
    public static final String DESCRIPTION = "Each of your turns, the first 3 times an allied minion attacks, give the minion +1/+0/+1.";

    @Override
    public Tooltip getTooltip() {
        return new Tooltip("Berserker", DESCRIPTION, List::of);
    }

    @Override
    public List<Effect> getEffects() {
        return List.of(new EffectOnAttackStats());
    }

    public static class EffectOnAttackStats extends Effect {
        // required for reflection
        public EffectOnAttackStats() {
            super(DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onListenEventWhileInPlay(Event event) {
            if (event instanceof EventMinionAttack && this.owner.board.getCurrentPlayerTurn() == this.owner.team && ((EventMinionAttack) event).m1.team == this.owner.team) {
                return new ResolverWithDescription(DESCRIPTION, perTurnCounter("buff").limit(3, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect buff = new Effect("+1/+0/+1 (from passive).", EffectStats.builder()
                                .change(Stat.ATTACK, 1)
                                .change(Stat.HEALTH, 1)
                                .build());
                        this.resolve(b, rq, el, new AddEffectResolver(((EventMinionAttack) event).m1, buff));
                    }
                }));
            } else {
                return null;
            }
        }

        @Override
        public double getPresenceValue(int refs) {
            return 2;
        }
    }
}
