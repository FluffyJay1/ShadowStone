package server.card.cardpack.basic;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.Effect;

import java.util.List;

public class Knight extends MinionText {
    public static final String NAME = "Knight";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.SWORDPALADIN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/knight.png",
            CRAFT, RARITY, 1, 1, 1, 1, true, Knight.class,
            new Vector2f(), -1, EventAnimationDamageSlash.class,
            List::of);

    @Override
    protected List<Effect> getSpecialEffects() {
        return List.of();
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
