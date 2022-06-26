package server.card.cardset.standard.neutral;

import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;

import java.util.List;

public class ServantOfDarkness extends MinionText {
    public static final String NAME = "Servant of Darkness";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.LEGENDARY;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/standard/servantofdarkness.png",
            CRAFT, TRAITS, RARITY, 5, 13, 6, 13, true, ServantOfDarkness.class,
            new Vector2f(150, 167), 1.3, new EventAnimationDamageSlash(),
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
