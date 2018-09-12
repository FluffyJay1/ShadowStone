package server;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import server.card.Card;
import server.card.CardStatus;
import server.card.Deck;
import server.card.Hand;
import server.card.Leader;
import server.card.Minion;
import server.card.effect.EffectStats;
import server.card.unleashpower.*;

public class Player {
	public Board board;
	public Deck deck;
	public Hand hand;
	public int team, mana = 3, maxmana = 3, maxmaxmana = 10; // don't ask
	public boolean unleashAllowed = true;
	public UnleashPower unleashPower;

	public Player(Board board, int team) {
		this.board = board;
		this.team = team;
		this.deck = new Deck(board, team);
		this.hand = new Hand(board, team);
		// this.unleashPower = new UnleashImbueMagic(board, team);
	}

	public void update(double frametime) {
		this.hand.update(frametime);
		if (this.unleashPower != null) {
			this.unleashPower.update(frametime);
		}
	}

	public void draw(Graphics g) {
		if (this.unleashPower != null) {
			this.unleashPower.draw(g);
			if (this.canUnleash()) {
				g.setColor(Color.cyan);
				g.drawOval(
						(float) (this.unleashPower.pos.x - UnleashPower.UNLEASH_POWER_RADIUS * this.unleashPower.scale),
						(float) (this.unleashPower.pos.y - UnleashPower.UNLEASH_POWER_RADIUS * this.unleashPower.scale),
						(float) (UnleashPower.UNLEASH_POWER_RADIUS * 2 * this.unleashPower.scale),
						(float) (UnleashPower.UNLEASH_POWER_RADIUS * 2 * this.unleashPower.scale));
				g.setColor(Color.white);
			}
		}
	}

	// uh
	public boolean canPlayCard(Card c) {
		return c != null && this.board.currentplayerturn == this.team && c.conditions()
				&& this.mana >= c.finalStatEffects.getStat(EffectStats.COST) && c.status.equals(CardStatus.HAND);
	}

	public boolean canUnleashCard(Card c) {
		return c instanceof Minion && !(c instanceof Leader) && ((Minion) c).isInPlay() && c.team == this.team
				&& this.canUnleash();
	}

	public boolean canUnleash() {
		return this.unleashAllowed
				&& this.unleashPower.unleashesThisTurn < this.unleashPower.finalStatEffects
						.getStat(EffectStats.ATTACKS_PER_TURN)
				&& this.mana >= this.unleashPower.finalStatEffects.getStat(EffectStats.COST)
				&& this.board.currentplayerturn == this.team;
	}

	public void printHand() {
		System.out.println("Hand " + this.team + ":");
		for (Card c : this.hand.cards) {
			System.out.print(c.id + " ");
		}
		System.out.println();
	}
}
