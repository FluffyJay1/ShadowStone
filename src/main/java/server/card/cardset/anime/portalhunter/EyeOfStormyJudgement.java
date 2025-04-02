package server.card.cardset.anime.portalhunter;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageMagicHit;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.resolver.DamageResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class EyeOfStormyJudgement extends AmuletText {
    public static final String NAME = "Eye of Stormy Judgement";
    private static final String AURA_DESCRIPTION = "<b>Aura</b>: Allied minions have \"<b>Strike</b>: Deal 1 damage to the enemy.\"";
    public static final String DESCRIPTION = "<b>Countdown(1)</b>.\n" + AURA_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.PORTALHUNTER;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, () -> new Animation("card/anime/eyeofstormyjudgement.png"),
            CRAFT, TRAITS, RARITY, 3, EyeOfStormyJudgement.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.AURA, Tooltip.STRIKE),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectAura(AURA_DESCRIPTION, 1, true, false, new EffectEyeOfStormyJudgementStrike()) {
                    @Override
                    public boolean applyConditions(Card cardToApply) {
                        return cardToApply instanceof Minion;
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        return 4 * AI.VALUE_PER_DAMAGE; // idk
                    }
                },
                new Effect("<b>Countdown(1)</b>.", EffectStats.builder()
                        .set(Stat.COUNTDOWN, 1)
                        .build()
                )
        );
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }

    public static class EffectEyeOfStormyJudgementStrike extends Effect {
        private static final String EFFECT_DESCRIPTION = "<b>Strike</b>: Deal 1 damage to the enemy (from <b>" + NAME + "'s Aura</b>).";
        public EffectEyeOfStormyJudgementStrike() {
            super(EFFECT_DESCRIPTION);
        }

        @Override
        public ResolverWithDescription strike(Minion target) {
            return new ResolverWithDescription(EFFECT_DESCRIPTION, new DamageResolver(this, target, 1, true, new EventAnimationDamageMagicHit().toString()));
        }

        @Override
        public double getPresenceValue(int refs) {
            return AI.VALUE_PER_DAMAGE * 1;
        }
    }
}
