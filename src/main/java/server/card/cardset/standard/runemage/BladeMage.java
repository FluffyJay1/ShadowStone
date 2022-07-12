package server.card.cardset.standard.runemage;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
import server.card.effect.common.EffectSpellboostDiscount;

import java.util.List;

public class BladeMage extends MinionText {
    public static final String NAME = "Blade Mage";
    public static final String DESCRIPTION = "<b>Storm</b>.\n" + EffectSpellboostDiscount.DESCRIPTION;
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/standard/blademage.png",
            CRAFT, TRAITS, RARITY, 5, 2, 2, 2, true, BladeMage.class,
            new Vector2f(182, 145), 1.4, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.STORM, Tooltip.SPELLBOOST),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new Effect("<b>Storm</b>.", EffectStats.builder()
                .set(Stat.STORM, 1)
                .build()),
                new EffectSpellboostDiscount());
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
