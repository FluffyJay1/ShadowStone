package server.card;

import java.util.ArrayList;

import cardpack.basic.*;
import server.Board;

public class Deck {
	Board board;
	public ArrayList<Card> cards;
	public int team;

	public Deck(Board board, int team) {
		this.board = board;
		this.team = team;
		this.cards = new ArrayList<Card>();
		for (int i = 0; i < 20; i++) {
			cards.add(new Goblin(board));
			cards.add(new Fighter(board));
			cards.add(new Fireball(board));
		}
		this.shuffle();
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
