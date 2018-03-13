package server.card;

import java.util.ArrayList;

import server.Board;

public class Hand { // its just a list of cards aaaaaa
	public static final int DEFAULT_MAX_SIZE = 10;
	public ArrayList<Card> cards;
	public int maxsize;
	public Board board;

	public Hand(Board board) {
		this.board = board;
		this.maxsize = DEFAULT_MAX_SIZE;
	}
}
