package client.tooltip;

import server.card.*;

// used to store metadata on a card
public abstract class TooltipCard extends Tooltip {
    public final Class<? extends Card> cardClass;
    public final int cost;
    public final ClassCraft craft;

    public TooltipCard(String name, String description, String imagepath, ClassCraft craft, int cost,
            Class<? extends Card> cardClass, Tooltip... references) {
        super(name, cost + "-cost " + craft.toString() + " " + description, imagepath, references);
        this.craft = craft;
        this.cost = cost;
        this.cardClass = cardClass;
    }
}
