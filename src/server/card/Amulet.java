package server.card;

import client.tooltip.*;
import server.Board;
import server.card.effect.Effect;

public class Amulet extends BoardObject {
	public Amulet(Board b, TooltipAmulet tooltip) {
		super(b, tooltip);
		this.addBasicEffect(new Effect(0, "", tooltip.cost));
	}
}
