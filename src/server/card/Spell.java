package server.card;

import java.util.LinkedList;

import client.tooltip.*;
import server.Board;
import server.card.effect.Effect;

public class Spell extends Card { // yea

	public Spell(Board board, int team, TooltipSpell tooltip) {
		super(board, team, tooltip);
		this.addBasicEffect(new Effect(0, "", tooltip.cost));
	}

	public String toString() {
		return "spell " + this.id + " " + this.cardPosToString() + " ";
	}

}
