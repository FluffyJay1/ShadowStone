package server.card;

import java.util.LinkedList;

import server.Board;

public class Spell extends Card { // yea

	public Spell(Board board, CardStatus status, int cost, String name, String text, String imagepath, int id) {
		super(board, status, cost, name, text, imagepath, id);
	}

}
