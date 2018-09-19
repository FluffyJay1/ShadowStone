package client.tooltip;

import server.card.ClassCraft;

public class TooltipAmulet extends TooltipCard {
	public TooltipAmulet(String name, String description, String imagepath, ClassCraft craft, int cost, int id,
			Tooltip... references) {
		super(name, "amulet\n \n" + description, imagepath, craft, cost, id, references);
	}
}
