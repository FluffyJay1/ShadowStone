package server;

import server.card.Deck;
import server.card.Hand;

public class Player {
	public Board board;
	public Deck deck;
	public Hand hand;
	public int team;

	public Player(Board board, int team) {
		this.board = board;
		this.team = team;
		this.deck = new Deck(board);
		this.hand = new Hand(board);
	}

}
