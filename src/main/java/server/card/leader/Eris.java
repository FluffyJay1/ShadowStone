package server.card.leader;

import client.tooltip.TooltipMinion;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.LeaderText;
import server.card.effect.Effect;

import java.util.List;

public class Eris extends LeaderText {
    public static final String NAME = "Eris";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, "", "res/leader/eris.png",
            CRAFT, TRAITS, RARITY, 0, 0, 0, 25, false, Eris.class,
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
