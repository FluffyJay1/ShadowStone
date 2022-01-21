package client.tooltip;

import org.newdawn.slick.geom.Vector2f;
import server.card.*;

public class TooltipSpell extends TooltipCard {
    public TooltipSpell(String name, String description, String imagepath, ClassCraft craft, int cost,
            Class<? extends Card> cardClass, Tooltip... references) {
        super(name, "spell\n \n" + description, imagepath, craft, cost, cardClass, new Vector2f(), -1, references);
    }
}
