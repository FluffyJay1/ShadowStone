package gamemode.dungeonrun.passive;

import java.util.List;

import client.tooltip.Tooltip;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageEnergyBeam;
import gamemode.dungeonrun.Passive;
import server.card.effect.Effect;
import server.resolver.BlastResolver;
import server.resolver.meta.ResolverWithDescription;

public class TurnEndLategameBlast extends Passive {
    public static final String DESCRIPTION = "At the end of your turn, if you have 10 or more mana orbs, <b>Blast(4)</b>.";

    @Override
    public Tooltip getTooltip() {
        return new Tooltip("Yamato Cannon", DESCRIPTION, () -> List.of(Tooltip.BLAST));
    }

    @Override
    public List<Effect> getEffects() {
        return List.of(new EffectTurnEndLategameBlast());
    }
    
    public static class EffectTurnEndLategameBlast extends Effect {
        // required for reflection
        public EffectTurnEndLategameBlast() {
            super(DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onTurnEndAllied() {
            if (owner.player.maxmana >= 10) {
                return new ResolverWithDescription(DESCRIPTION, new BlastResolver(this, 4, new EventAnimationDamageEnergyBeam()));
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
