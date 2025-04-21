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
import server.resolver.Resolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class AfterDamageArmor extends Passive {
    public static final String DESCRIPTION = "After your leader takes damage for the first time each turn, gain +1 <b>Armor</b> until the end of the turn.";

    @Override
    public Tooltip getTooltip() {
        return new Tooltip("Reactive Armor", DESCRIPTION, () -> List.of(Tooltip.ARMOR));
    }

    @Override
    public List<Effect> getEffects() {
        return List.of(new EffectAfterDamageArmor());
    }

    public static class EffectAfterDamageArmor extends Effect {
        // required for reflection
        public EffectAfterDamageArmor() {
            super(DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onDamaged(int damage) {
            if (damage > 0) {
                return new ResolverWithDescription(DESCRIPTION, perTurnCounter("armor").limit(1, new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Effect buff = new Effect("+1 <b>Armor</b> (from passive).", EffectStats.builder()
                                .change(Stat.ARMOR, 1)
                                .build(),
                                e -> e.setUntilTurnEnd(0, 1));
                        this.resolve(b, rq, el, new AddEffectResolver(owner, buff));
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
