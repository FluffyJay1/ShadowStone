package client.tooltip;

public class TooltipAmulet extends Tooltip {
	public int cost;

	public TooltipAmulet(String name, String description, int cost, Tooltip... references) {
		super(name, cost + "-cost amulet\n \n" + description, references);
		this.cost = cost;
	}
}
