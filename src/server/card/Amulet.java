package server.card;

import client.tooltip.*;
import server.Board;
import server.card.effect.Effect;

public class Amulet extends BoardObject {
	public Amulet(Board b, int team, TooltipAmulet tooltip) {
		super(b, team, tooltip);
		this.addBasicEffect(new Effect(0, "", tooltip.cost));
	}
}
