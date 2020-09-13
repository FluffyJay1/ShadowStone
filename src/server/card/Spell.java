package server.card;

import client.tooltip.*;
import server.*;
import server.card.effect.*;

public class Spell extends Card { // yea

	public Spell(Board board, TooltipSpell tooltip) {
		super(board, tooltip);
		this.addEffect(true, new Effect("", tooltip.cost));
	}

}
