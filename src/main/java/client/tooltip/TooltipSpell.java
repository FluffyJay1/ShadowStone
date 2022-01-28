package client.tooltip;

import org.newdawn.slick.geom.Vector2f;
import server.card.*;

import java.util.List;
import java.util.function.Supplier;

public class TooltipSpell extends TooltipCard {
    public TooltipSpell(String name, String description, String imagepath, ClassCraft craft, int cost,
                        Class<? extends Card> cardClass, Supplier<List<Tooltip>> references) {
        super(name, "spell\n \n" + description, imagepath, craft, cost, cardClass, new Vector2f(), -1, references);
    }
}
