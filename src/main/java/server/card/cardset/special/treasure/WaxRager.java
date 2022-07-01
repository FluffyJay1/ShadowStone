package server.card.cardset.special.treasure;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;
import server.card.effect.common.EffectLastWordsSummon;

import java.util.List;

public class WaxRager extends MinionText {
    public static final String NAME = "Wax Rager";
    public static final String DESCRIPTION = "<b>Last Words</b>: Summon a <b>Wax Rager</b>.";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/special/waxrager.png",
            CRAFT, TRAITS, RARITY, 3, 5, 1, 1, true, WaxRager.class,
            new Vector2f(147, 148), 1.3, new EventAnimationDamageSlash(),
            () -> List.of(Tooltip.LASTWORDS),
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of(new EffectLastWordsSummon(DESCRIPTION, new WaxRager(), 1));
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
