package server.card;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;

import server.Board;

public class Hand { // its just a list of cards aaaaaa
	public static final int DEFAULT_MAX_SIZE = 10;
	public ArrayList<Card> cards;
	public int maxsize;
	public int team;
	public Board board;

	public Hand(Board board, int team) {
		this.board = board;
		this.maxsize = DEFAULT_MAX_SIZE;
		this.cards = new ArrayList<Card>();
		this.team = team;
	}

	public void update(double frametime) {
		for (Card c : this.cards) {
			c.update(frametime);
		}
	}

	public void draw(Graphics g) {

	}
}
