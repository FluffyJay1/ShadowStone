package server.card.cardset.basic.shadowshaman;

import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.card.CardRarity;
import server.card.ClassCraft;
import server.card.MinionText;
import server.card.effect.Effect;

import java.util.List;

public class Zombie extends MinionText {
    public static final String NAME = "Zombie";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/zombie.png",
            CRAFT, RARITY, 2, 2, 1, 2, true, Zombie.class,
            new Vector2f(155, 120), 1.5, EventAnimationDamageSlash.class,
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
