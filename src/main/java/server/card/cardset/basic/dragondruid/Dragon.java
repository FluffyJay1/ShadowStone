package server.card.cardset.basic.dragondruid;

import client.tooltip.Tooltip;
import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;

import java.util.List;

public class Dragon extends MinionText {
    public static final String NAME = "Dragon";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/dragon.png",
            CRAFT, TRAITS, RARITY, 5, 5, 2, 5, true, Dragon.class,
            new Vector2f(128, 187), 1.2, EventAnimationDamageSlash.class,
            List::of);

    @Override
    protected List<Effect> getSpecialEffects() {
        return null;
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
