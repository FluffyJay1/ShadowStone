package server.card.cardset.basic.swordpaladin;

import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.card.*;
import server.card.effect.Effect;

import java.util.List;

public class Knight extends MinionText {
    public static final String NAME = "Knight";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of(CardTrait.OFFICER);
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/knight.png",
            CRAFT, TRAITS, RARITY, 1, 1, 1, 1, true, Knight.class,
            new Vector2f(), -1, new EventAnimationDamageSlash(),
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
