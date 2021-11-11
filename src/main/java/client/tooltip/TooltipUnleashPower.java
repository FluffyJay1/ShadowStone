package client.tooltip;

import server.card.*;

public class TooltipUnleashPower extends TooltipCard {

    public TooltipUnleashPower(String name, String description, String imagepath, ClassCraft craft, int cost,
            Class<? extends Card> cardClass, Tooltip... references) {
        super(name, "unleash power\n \n" + description, imagepath, craft, cost, cardClass, references);
    }

}
