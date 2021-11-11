package client.tooltip;

import server.card.*;

public class TooltipSpell extends TooltipCard {
    public TooltipSpell(String name, String description, String imagepath, ClassCraft craft, int cost,
            Class<? extends Card> cardClass, Tooltip... references) {
        super(name, "spell\n \n" + description, imagepath, craft, cost, cardClass, references);
    }
}
