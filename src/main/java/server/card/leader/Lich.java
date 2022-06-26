package server.card.leader;

import client.tooltip.TooltipMinion;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.CardTrait;
import server.card.ClassCraft;
import server.card.LeaderText;
import server.card.effect.Effect;

import java.util.List;

public class Lich extends LeaderText {
    public static final String NAME = "Lich";
    public static final String DESCRIPTION = "Gonna have your mana.";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final List<CardTrait> TRAITS = List.of();
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/leader/lich.png",
            CRAFT, TRAITS, RARITY, 0, 0, 0, 25, false, Lich.class,
            new Vector2f(), -1, null,
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
