package gamemode.dungeonrun.passive;

import java.util.List;

import client.tooltip.Tooltip;
import gamemode.dungeonrun.Passive;
import server.ai.AI;
import server.card.Card;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

public class AuraLeftAttackLifesteal extends Passive {
    public static final String DESCRIPTION = "<b>Aura</b>: Your minion in the most-clockwise position has +1/+0/+0 and <b>Lifesteal</b>.";

    @Override
    public Tooltip getTooltip() {
        return new Tooltip("Vladimir's Aura", DESCRIPTION, () -> List.of(Tooltip.AURA, Tooltip.LIFESTEAL));
    }

    @Override
    public List<Effect> getEffects() {
        return List.of(new EffectAuraLeftAttackLifesteal());
    }
    
    public static class EffectAuraLeftAttackLifesteal extends EffectAura {
        // required for reflection
        public EffectAuraLeftAttackLifesteal() {
            super(DESCRIPTION, 1, true, false,
                new Effect("+1/+0/+0 and <b>Lifesteal</b> (from passive).", EffectStats.builder()
                        .change(Stat.ATTACK, 1)
                        .set(Stat.LIFESTEAL, 1)
                        .build()));
        }

        @Override
        public boolean applyConditions(Card cardToApply) {
            return cardToApply instanceof Minion && owner.player.getPlayArea().stream().filter(bo -> bo instanceof Minion).findFirst().map(bo -> cardToApply == bo).orElse(false);
        }

        @Override
        public double getPresenceValue(int refs) {
            return AI.valueForBuff(1, 0, 0) + AI.VALUE_OF_LIFESTEAL;
        }
    }
}
