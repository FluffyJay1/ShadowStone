package client.tooltip;

import server.card.*;

public class TooltipAmulet extends TooltipCard {
    public TooltipAmulet(String name, String description, String imagepath, ClassCraft craft, int cost,
            Class<? extends Card> cardClass, Tooltip... references) {
        super(name, "amulet\n \n" + description, imagepath, craft, cost, cardClass, references);
    }
}
