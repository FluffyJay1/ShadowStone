package server.card.cardset.standard.havenpriest;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;

import org.newdawn.slick.geom.Vector2f;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

import java.util.List;

public class Aegina extends MinionText {
    public static final String NAME = "Aegina";
    public static final String DESCRIPTION = "<b>Aura</b>: Your leader has +3 <b>Armor</b>.";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/standard/aegina.png"),
            CRAFT, TRAITS, RARITY, 3, 3, 1, 3, true, Aegina.class,
            new Vector2f(148, 161), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.AURA, Tooltip.ARMOR),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectAura(DESCRIPTION, 1, false, false, true, false,
                new Effect("+3 <b>Armor</b> (from <b>" + NAME + "</b>).", EffectStats.builder()
                        .change(Stat.ARMOR, 3)
                        .build())) {
            @Override
            public boolean applyConditions(Card cardToApply) {
                return cardToApply instanceof Leader;
            }

            @Override
            public double getPresenceValue(int refs) {
                return AI.VALUE_PER_ARMOR * 3; // idk
            }
        });
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
