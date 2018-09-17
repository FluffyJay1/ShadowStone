package server.card;

import java.util.LinkedList;

import client.tooltip.*;
import server.Board;
import server.card.effect.Effect;

public class Spell extends Card { // yea

	public Spell(Board board, CardStatus status, int cost, TooltipSpell tooltip, String imagepath, int team,
			ClassCraft craft, int id) {
		super(board, status, tooltip, imagepath, team, craft, id);
		this.addBasicEffect(new Effect(0, "", cost));
	}

	public String toString() {
		return "spell " + this.id + " " + this.cardPosToString() + " ";
	}

}
