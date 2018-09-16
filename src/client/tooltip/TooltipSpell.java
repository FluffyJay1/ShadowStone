package client.tooltip;

import server.card.ClassCraft;

public class TooltipSpell extends TooltipCard {
	public TooltipSpell(String name, String description, ClassCraft craft, int cost, Tooltip... references) {
		super(name, "spell\n \n" + description, craft, cost, references);
	}
}
