package server.card;

import java.util.LinkedList;

import client.tooltip.*;
import server.Board;
import server.card.effect.Effect;

public class Spell extends Card { // yea

	public Spell(Board board, TooltipSpell tooltip) {
		super(board, tooltip);
		this.addBasicEffect(new Effect(0, "", tooltip.cost));
	}

	public String toString() {
		return "spell " + this.id + " " + this.cardPosToString() + " ";
	}

}
