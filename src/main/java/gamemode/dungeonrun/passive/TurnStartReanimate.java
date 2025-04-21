package gamemode.dungeonrun.passive;

import java.util.List;

import client.tooltip.Tooltip;
import gamemode.dungeonrun.Passive;
import server.card.effect.Effect;
import server.resolver.ReanimateResolver;
import server.resolver.meta.ResolverWithDescription;

public class TurnStartReanimate extends Passive {
    public static final String DESCRIPTION = "At the start of your turn, <b>Reanimate(2)</b>.";

    @Override
    public Tooltip getTooltip() {
        return new Tooltip("", DESCRIPTION, () -> List.of(Tooltip.REANIMATE));
    }

    @Override
    public List<Effect> getEffects() {
        return List.of(new EffectTurnStartReanimate());
    }
    
    public static class EffectTurnStartReanimate extends Effect {
        // required for reflection
        public EffectTurnStartReanimate() {
            super(DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onTurnStartAllied() {
            return new ResolverWithDescription(DESCRIPTION, new ReanimateResolver(owner.player, 2, -1));
        }

        @Override
        public double getPresenceValue(int refs) {
            return 2;
        }
    }
}
