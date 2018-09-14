package client.tooltip;

public class TooltipSpell extends Tooltip {
	public int cost;

	public TooltipSpell(String name, String description, int cost, Tooltip... references) {
		super(name, cost + "-cost spell\n \n" + description, references);
		this.cost = cost;
	}
}
