package server.card;

import java.util.LinkedList;

import server.Board;
import server.card.effect.Effect;

public class Spell extends Card { // yea

	public Spell(Board board, CardStatus status, int cost, String name, String text, String imagepath, int team,
			int id) {
		super(board, status, name, text, imagepath, team, id);
		this.addBasicEffect(new Effect(0, "", cost));
	}

	public String toString() {
		return "spell " + this.id + " " + this.cardPosToString() + " ";
	}

}
