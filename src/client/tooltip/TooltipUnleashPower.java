package client.tooltip;

import server.card.ClassCraft;

public class TooltipUnleashPower extends TooltipCard {

	public TooltipUnleashPower(String name, String description, String imagepath, ClassCraft craft, int cost, int id,
			Tooltip... references) {
		super(name, "unleash power\n \n" + description, imagepath, craft, cost, id, references);
	}

}
