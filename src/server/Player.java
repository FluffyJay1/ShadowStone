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
		if (this.team == 1) {
			for (int i = 0; i < this.hand.cards.size(); i++) {
				Card c = this.hand.cards.get(i);
				c.targetpos.set((int) (((i) - (this.hand.cards.size()) / 2.) * 500 / this.hand.cards.size() + 1500),
						1000);
				c.scale = 0.5;
				c.draw(g);
			}
		}
	}
}
