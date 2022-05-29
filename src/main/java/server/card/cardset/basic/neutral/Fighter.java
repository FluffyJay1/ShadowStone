package server.card.cardset.basic.neutral;

import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.card.*;
import server.card.effect.Effect;

import java.util.List;

public class Fighter extends MinionText {
    public static final String NAME = "Fighter";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/fighter.png",
            CRAFT, TRAITS, RARITY, 2, 2, 1, 2, true, Fighter.class,
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
