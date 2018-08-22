package server;

import org.newdawn.slick.Graphics;

import server.card.Card;
import server.card.CardStatus;
import server.card.Deck;
import server.card.Hand;
import server.card.Leader;
import server.card.Minion;
import server.card.effect.EffectStats;

public class Player {
	public Board board;
	public Deck deck;
	public Hand hand;
	public int team, mana = 1, maxmana = 1, maxmaxmana = 10; // don't ask
	public boolean unleashedThisTurn, canUnleash;

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

	// uh
	public boolean canPlayCard(Card c) {
		return c != null && this.board.currentplayerturn == this.team && c.conditions()
				&& this.mana >= c.finalStatEffects.getStat(EffectStats.COST);
	}

	public boolean canUnleashCard(Card c) {
		return c instanceof Minion && !(c instanceof Leader) && c.status == CardStatus.BOARD && c.team == 1
				&& !this.unleashedThisTurn && this.mana >= 2 && c.alive == true;
	}

	public void printHand() {
		System.out.println("Hand " + this.team + ":");
		for (Card c : this.hand.cards) {
			System.out.print(c.id + " ");
		}
		System.out.println();
	}
}
