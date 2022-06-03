package server.card.cardset.basic.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageClaw;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.common.EffectSpellboostDiscount;

import java.util.List;

public class FlameDestroyer extends MinionText {
    public static final String NAME = "Flame Destroyer";
    public static final String DESCRIPTION = EffectSpellboostDiscount.DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.SILVER;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/flamedestroyer.png",
            CRAFT, TRAITS, RARITY, 10, 7, 3, 7, true, FlameDestroyer.class,
            new Vector2f(150, 200), 1.2, EventAnimationDamageClaw.class,
            () -> List.of(Tooltip.SPELLBOOST));

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectSpellboostDiscount());
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
