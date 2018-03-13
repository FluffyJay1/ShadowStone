package server.card;

import java.util.ArrayList;

import cardpack.basic.*;
import server.Board;

public class Deck {
	Board board;
	public ArrayList<Card> cards;

	public Deck(Board board) {
		this.board = board;
		for (int i = 0; i < 20; i++) {
			cards.add(new Goblin(board));
			cards.add(new Fighter(board));
		}
	}

	public void shuffle() {
		ArrayList<Card> newcards = new ArrayList<Card>();
		while (!this.cards.isEmpty()) {
			int randomindex = (int) (Math.random() * this.cards.size());
			newcards.add(this.cards.remove(randomindex));
		}
		this.cards = newcards;
	}
}
