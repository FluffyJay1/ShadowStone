package server.card;

import java.util.*;

import server.*;

public class Deck {
	Board board;
	public ArrayList<Card> cards;
	public int team;

	public Deck(Board board, int team) {
		this.board = board;
		this.team = team;
		this.cards = new ArrayList<Card>();
	}

	public void updatePositions() {
		for (int i = 0; i < this.cards.size(); i++) {
			this.cards.get(i).cardpos = i;
		}
	}
}
