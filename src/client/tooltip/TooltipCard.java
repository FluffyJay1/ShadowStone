package client.tooltip;

import server.card.ClassCraft;

public abstract class TooltipCard extends Tooltip {
	public int cost, id;
	public ClassCraft craft;

	public TooltipCard(String name, String description, String imagepath, ClassCraft craft, int cost, int id,
			Tooltip... references) {
		super(name, cost + "-cost " + craft.toString() + " " + description, imagepath, references);
		this.craft = craft;
		this.cost = cost;
		this.id = id;
	}
}
