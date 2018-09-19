package client.tooltip;

import server.card.ClassCraft;

public class TooltipMinion extends TooltipCard {
	public int attack, magic, health;
	public boolean basicUnleash;

	public TooltipMinion(String name, String description, String imagepath, ClassCraft craft, int cost, int attack,
			int magic, int health, boolean basicUnleash, int id, Tooltip... references) {
		super(name,
				"minion\nA:" + attack + ", M:" + magic + ", H:" + health + "\n \n" + (basicUnleash
						? "<b> Unleash: </b> Deal X damage to an enemy minion. X equals this minion's magic.\n" : "")
						+ description,
				imagepath, craft, cost, id);
		this.basicUnleash = basicUnleash;
		if (basicUnleash) {
			this.references = new Tooltip[references.length + 1];
			this.references[0] = Tooltip.UNLEASH;
			System.arraycopy(references, 0, this.references, 1, references.length);
		} else {
			this.references = references;
		}
		this.attack = attack;
		this.magic = magic;
		this.health = health;
	}
}
