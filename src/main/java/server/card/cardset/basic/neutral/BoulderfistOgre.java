package server.card.cardset.basic.neutral;

import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;

import java.util.List;

public class BoulderfistOgre extends MinionText {
    public static final String NAME = "Boulderfist Ogre";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "card/basic/boulderfistogre.png",
            CRAFT, TRAITS, RARITY, 6, 6, 0, 7, true, BoulderfistOgre.class,
            new Vector2f(158, 164), 1.5, new EventAnimationDamageSlash(),
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of();
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
