package client.tooltip;

import server.card.ClassCraft;

public class TooltipSpell extends TooltipCard {
	public TooltipSpell(String name, String description, String imagepath, ClassCraft craft, int cost, int id,
			Tooltip... references) {
		super(name, "spell\n \n" + description, imagepath, craft, cost, id, references);
	}
}
