package server.card.cardset.indie.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;
import client.ui.Animation;

import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.resolver.SpendResolver;
import server.resolver.meta.ResolverWithDescription;

import java.util.List;

public class EmotionSad extends AmuletText {
    public static final String NAME = "Emotion: Sad";
    private static final String AURA_DESCRIPTION = "<b>Aura</b>: Allies have -1/+0/+0 and +1 <b>Armor</b>.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + AURA_DESCRIPTION + "\nAt the start of your turn, <b>Spend</b> 1 mana orb.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION,
            () -> new Animation("card/indie/emotionsad.png", new Vector2f(3, 1), 0, 0, Image.FILTER_NEAREST,
                    anim -> {
                        anim.play = true;
                        anim.loop = true;
                        anim.setFrameInterval(0.2);
                    }),
            CRAFT, TRAITS, RARITY, 3, EmotionSad.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.AURA, Tooltip.ARMOR, Tooltip.SPEND),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectAura(AURA_DESCRIPTION, 1, true, false, true, false,
                        new Effect("-1/+0/+0 and +1 <b>Armor</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                                .change(Stat.ATTACK, -1)
                                .change(Stat.ARMOR, 1)
                                .build())) {
                    @Override
                    public boolean applyConditions(Card cardToApply) {
                        return cardToApply instanceof Minion;
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        return (AI.valueForBuff(-1, 0, 0) + AI.VALUE_PER_ARMOR) * 3 - 1; // idk
                    }
                },
                new Effect("<b>Countdown(3)</b>. At the start of your turn, <b>Spend</b> 1 mana orb.", EffectStats.builder()
                        .set(Stat.COUNTDOWN, 3)
                        .build()
                ) {
                    public ResolverWithDescription onTurnStartAllied() {
                        return new ResolverWithDescription("At the start of your turn, <b>Spend</b> 1 mana orb.", new SpendResolver(this, 1, null));
                    };
                }
        );
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
