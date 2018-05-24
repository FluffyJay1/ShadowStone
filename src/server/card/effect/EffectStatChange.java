package server.card.effect;

import server.card.Card;

public class EffectStatChange extends Effect {
	public static final int ID = -1;

	public EffectStatChange(Card owner, String description) {
		super(owner, ID, description);
	}

	public String toString() {
		String ret = this.id + " " + this.description + " set ";
		for (int i = 0; i < this.set.stats.length; i++) {
			if (this.set.use[i]) {
				ret += i + " " + this.set.stats[i] + " ";
			}
		}
		ret += "change ";
		for (int i = 0; i < this.change.stats.length; i++) {
			if (this.change.use[i]) {
				ret += i + " " + this.change.stats[i] + " ";
			}
		}
		return ret;
	}
}
