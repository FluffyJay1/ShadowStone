package server.card.cardset.basic.runemage;

import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;

import java.util.List;

public class ClayGolem extends MinionText {
    public static final String NAME = "Clay Golem";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.RUNEMAGE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/claygolem.png",
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, true, ClayGolem.class,
            new Vector2f(125, 140), 1.4, EventAnimationDamageSlash.class,
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
