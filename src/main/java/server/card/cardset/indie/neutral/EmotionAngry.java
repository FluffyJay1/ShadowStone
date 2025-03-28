package server.card.cardset.indie.neutral;

import client.tooltip.Tooltip;
import client.tooltip.TooltipAmulet;

import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

import java.util.List;

public class EmotionAngry extends AmuletText {
    public static final String NAME = "Emotion: Angry";
    private static final String AURA_DESCRIPTION = "<b>Aura</b>: Allies have -1 <b>Armor</b>. Allied minions have +2/+0/+0.";
    public static final String DESCRIPTION = "<b>Countdown(3)</b>.\n" + AURA_DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipAmulet TOOLTIP = new TooltipAmulet(NAME, DESCRIPTION, "card/indie/emotionangry.png",
            CRAFT, TRAITS, RARITY, 3, EmotionAngry.class,
            new Vector2f(), -1,
            () -> List.of(Tooltip.COUNTDOWN, Tooltip.AURA, Tooltip.ARMOR),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(
                new EffectAura(AURA_DESCRIPTION, 1, true, false, true, false, new Effect("-1 <b>Armor</b> (from <b>" + NAME + "</b>).", EffectStats.builder().change(Stat.ARMOR, -1).build())) {
                    @Override
                    public boolean applyConditions(Card cardToApply) {
                        return cardToApply instanceof Minion;
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        return (AI.VALUE_PER_ARMOR * -1) * 3; // idk
                    }
                },
                new EffectAura(AURA_DESCRIPTION, 1, true, false, new Effect("+2/+0/+0 (from <b>" + NAME + "</b>).", EffectStats.builder().change(Stat.ATTACK, 2).build())) {
                    @Override
                    public boolean applyConditions(Card cardToApply) {
                        return cardToApply instanceof Minion;
                    }

                    @Override
                    public double getPresenceValue(int refs) {
                        return AI.valueForBuff(2, 0, 0) * 3; // idk
                    }
                },
                new Effect("<b>Countdown(3)</b>", EffectStats.builder()
                        .set(Stat.COUNTDOWN, 3)
                        .build()
                )
        );
    }

    @Override
    public TooltipAmulet getTooltip() {
        return TOOLTIP;
    }
}
