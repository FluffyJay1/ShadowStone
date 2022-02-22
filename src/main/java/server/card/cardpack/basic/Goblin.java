package server.card.cardpack.basic;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;

import java.util.List;

public class Goblin extends Minion {
    public static final String NAME = "Goblin";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.NEUTRAL;
    public static final CardRarity RARITY = CardRarity.BRONZE;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/goblin.png",
            CRAFT, RARITY, 1, 1, 0, 2, true, Goblin.class,
            new Vector2f(), -1, EventAnimationDamageSlash.class,
            List::of);

    public Goblin(Board b) {
        super(b, TOOLTIP);
    }
}
