package server.card.cardpack.basic;

import client.tooltip.TooltipMinion;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamageSlash;
import org.newdawn.slick.geom.Vector2f;
import server.Board;
import server.card.ClassCraft;
import server.card.Minion;

import java.util.List;

public class HolyflameTiger extends Minion {
    public static final String NAME = "Holyflame Tiger";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.HAVENPRIEST;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/holyflametiger.png",
            CRAFT, 4, 4, 1, 4, true, HolyflameTiger.class, new Vector2f(163, 128), 1.5, EventAnimationDamageSlash.class,
            List::of);

    public HolyflameTiger(Board b) {
        super(b, TOOLTIP);
    }
}
