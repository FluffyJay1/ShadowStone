package server.card.cardset.basic.forestrogue;

import client.ui.Animation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageOrbFall;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.card.*;
import server.card.effect.Effect;

import java.util.List;

public class Fairy extends MinionText {
    public static final String NAME = "Fairy";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.FORESTROGUE;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, () -> new Animation("card/basic/fairy.png"),
            CRAFT, TRAITS, RARITY, 1, 1, 1, 1, true, Fairy.class,
            new Vector2f(), -1, new EventAnimationDamageOrbFall(),
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
