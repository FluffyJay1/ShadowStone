package server.card.cardpack.basic;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;

import java.util.List;

public class Fighter extends Minion {
    public static final String NAME = "Fighter";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/fighter.png",
            CRAFT, RARITY, 2, 2, 1, 2, true, Fighter.class,
            new Vector2f(), -1, EventAnimationDamageSlash.class,
            List::of);

    public Fighter(Board b) {
        super(b, TOOLTIP);
    }
}
