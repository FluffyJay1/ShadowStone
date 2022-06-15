package server.card.leader;

import client.tooltip.TooltipMinion;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.LeaderText;
import server.card.effect.Effect;

import java.util.List;

public class Urias extends LeaderText {
    public static final String NAME = "Urias";
    public static final ClassCraft CRAFT = ClassCraft.BLOODWARLOCK;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, "", "res/leader/urias.png",
            CRAFT, TRAITS, RARITY, 0, 0, 0, 25, false, Urias.class,
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
