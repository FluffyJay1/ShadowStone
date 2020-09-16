package client.tooltip;

import server.card.*;

public abstract class TooltipCard extends Tooltip {
	public Class<? extends Card> cardClass;
	public int cost;
	public ClassCraft craft;

	public TooltipCard(String name, String description, String imagepath, ClassCraft craft, int cost,
			Class<? extends Card> cardClass, Tooltip... references) {
		super(name, cost + "-cost " + craft.toString() + " " + description, imagepath, references);
		this.craft = craft;
		this.cost = cost;
		this.cardClass = cardClass;
	}
}
