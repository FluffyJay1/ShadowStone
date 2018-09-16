package client.tooltip;

import server.card.ClassCraft;

public class TooltipAmulet extends TooltipCard {
	public TooltipAmulet(String name, String description, ClassCraft craft, int cost, Tooltip... references) {
		super(name, "amulet\n \n" + description, craft, cost, references);
	}
}
