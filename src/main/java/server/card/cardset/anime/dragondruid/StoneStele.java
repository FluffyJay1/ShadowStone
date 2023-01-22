package server.card.cardset.anime.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageEnergyBeamQuick;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.resolver.BlastResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class StoneStele extends AmuletText {
    public static final String NAME = "Stone Stele";
    private static final String AURA_DESCRIPTION = "<b>Aura</b>: Allied amulets have \"At the start of your turn, <b>Blast(1)</b>.\"";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + AURA_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "card/anime/stonestele.png",
            CRAFT, TRAITS, RARITY, 3, StoneStele.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.AURA, Tooltip.BLAST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectAura(AURA_DESCRIPTION, 1, true, false, new EffectStoneSteleResonance()) {
                    @Override
                    public boolean applyConditions(Card cardToApply) {
                        return cardToApply instanceof Amulet;
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        return 4; // idk
                    }
                },
                new Effect("<b>Countdown(3)</b>.", EffectStats.builder()
                        .set(Stat.COUNTDOWN, 3)
                        .build()
                )
        );
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }

    public static class EffectStoneSteleResonance extends Effect {
        private static final String EFFECT_DESCRIPTION = "At the start of your turn, <b>Blast(1)</b> (from <b>" + NAME + "'s Aura</b>).";
        public EffectStoneSteleResonance() {
            super(EFFECT_DESCRIPTION);
        }

        @Override
        public ResolverWithDescription onTurnStartAllied() {
            return new ResolverWithDescription(EFFECT_DESCRIPTION, new BlastResolver(this, 1, new EventAnimationDamageEnergyBeamQuick().toString()));
        }

        @Override
        public double getPresenceValue(int refs) {
            return AI.VALUE_PER_DAMAGE;
        }
    }
}
