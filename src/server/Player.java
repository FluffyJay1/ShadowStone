package server;

import org.newdawn.slick.Graphics;

import server.card.Card;
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
		this.deck = new Deck(board, team);
		this.hand = new Hand(board, team);
	}

	public void update(double frametime) {
		this.hand.update(frametime);
	}

	public void draw(Graphics g) {

	}
}
