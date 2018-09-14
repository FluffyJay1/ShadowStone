package client.tooltip;

public class TooltipMinion extends Tooltip {
	public int cost, attack, magic, health;

	public TooltipMinion(String name, String description, int cost, int attack, int magic, int health,
			boolean basicUnleash, Tooltip... references) {
		super(name,
				cost + "-cost minion\nA:" + attack + ", M:" + magic + ", H:" + health + "\n \n" + (basicUnleash
						? "<b> Unleash: </b> Deal X damage to an enemy minion. X equals this minion's magic.\n" : "")
						+ description);
		if (basicUnleash) {
			this.references = new Tooltip[references.length + 1];
			this.references[0] = Tooltip.UNLEASH;
			System.arraycopy(references, 0, this.references, 1, references.length);
		} else {
			this.references = references;
		}
		this.cost = cost;
		this.attack = attack;
		this.magic = magic;
		this.health = health;
	}
}
