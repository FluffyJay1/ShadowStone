package client.tooltip;

import server.card.ClassCraft;

public abstract class TooltipCard extends Tooltip {
	int cost;
	ClassCraft craft;

	public TooltipCard(String name, String description, ClassCraft craft, int cost, Tooltip... references) {
		super(name, cost + "-cost " + craft.toString() + " " + description, references);
		this.craft = craft;
		this.cost = cost;
	}
}
