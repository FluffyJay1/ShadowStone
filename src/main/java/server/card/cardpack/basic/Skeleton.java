package server.card.cardpack.basic;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.*;

import java.util.List;

public class Skeleton extends Minion {
    public static final String NAME = "Skeleton";
    public static final String DESCRIPTION = "";
    public static final ClassCraft CRAFT = ClassCraft.SHADOWSHAMAN;
    public static final TooltipMinion TOOLTIP = new TooltipMinion(NAME, DESCRIPTION, "res/card/basic/skeleton.png",
            CRAFT, 1, 1, 1, 1, true, Skeleton.class, new Vector2f(), -1, null,
            List::of);

    public Skeleton(Board b) {
        super(b, TOOLTIP);
    }
}
