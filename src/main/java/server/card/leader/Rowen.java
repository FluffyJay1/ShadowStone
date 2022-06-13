package server.card.leader;

import client.tooltip.TooltipMinion;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.card.effect.Effect;

import java.util.List;

public class Rowen extends LeaderText {
    public static final String NAME = "Rowen";
    public static final ClassCraft CRAFT = ClassCraft.DRAGONDRUID;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, "", "res/leader/smile.png",
            CRAFT, TRAITS, RARITY, 0, 0, 0, 25, false, Rowen.class,
            new Vector2f(), -1, null,
            List::of,
            List.of());

    @Override
    protected List<Effect> getSpecialEffects() {
        return null;
    }

    @Override
    public TooltipMinion getTooltip() {
        return TOOLTIP;
    }
}
