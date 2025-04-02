package server.card.cardset.anime.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOrbFall;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

import java.util.List;

public class Paimon extends MinionText {
    public static final String NAME = "Paimon";
    private static final String AURA_DESCRIPTION = "<b>Aura</b>: The allied minion directly counter-clockwise to this has +0/+2/+0.";
    private static final String NONAURA_DESCRIPTION = "<b>Stealth</b>.";
    public static final String DESCRIPTION = NONAURA_DESCRIPTION + "\n" + AURA_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/anime/paimon.png"),
            CRAFT, TRAITS, RARITY, 3, 0, 2, 2, true, Paimon.class,
            new Vector2f(160, 145), 1.3, new EventAnimationDamageOrbFall(),
            () -> List.of(Tooltip.STEALTH, Tooltip.AURA),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectAura(AURA_DESCRIPTION, 1, true, false, new Effect("+0/+2/+0 (from <b>" + NAME + "</b>).", EffectStats.builder()
                        .change(Stat.MAGIC, 2)
                        .build())) {
                    @Override
                    public boolean applyConditions(Card cardToApply) {
                        return cardToApply instanceof Minion && cardToApply.getIndex() == this.owner.getIndex() + 1;
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        return AI.valueForBuff(0, 2, 0);
                    }
                },
                new Effect(NONAURA_DESCRIPTION, EffectStats.builder()
                        .set(Stat.STEALTH, 1)
                        .build())
        );
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
