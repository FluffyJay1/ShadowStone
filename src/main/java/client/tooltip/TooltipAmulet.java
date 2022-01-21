package client.tooltip;

import org.newdawn.slick.geom.Vector2f;
import server.card.*;

public class TooltipAmulet extends TooltipCard {
    public TooltipAmulet(String name, String description, String imagepath, ClassCraft craft, int cost,
                         Class<? extends Card> cardClass, Vector2f artFocusPos,
                         double artFocusScale, Tooltip... references) {
        super(name, "amulet\n \n" + description, imagepath, craft, cost, cardClass, artFocusPos, artFocusScale, references);
    }
}
