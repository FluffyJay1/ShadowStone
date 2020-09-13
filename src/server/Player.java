package server;

import server.card.*;
import server.card.effect.*;
import server.card.unleashpower.*;

public class Player {
	public Player realPlayer;
	public Board board;
	public Deck deck;
	public Hand hand;
	public int team, mana = 0, maxmana = 3, maxmaxmana = 10; // don't ask
	public boolean unleashAllowed = true;
	public Leader leader;
	public UnleashPower unleashPower;

	public Player(Board board, int team) {
		this.board = board;
		this.team = team;
		this.deck = new Deck(board, team);
		this.hand = new Hand(board, team);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.team).append(" ").append(this.mana).append(" ").append(this.maxmana).append(" ")
				.append(this.maxmaxmana).append(" ").append(this.unleashAllowed).append(" ");
		return sb.toString();
	}

	// uh
	public boolean canPlayCard(Card c) {
		return c != null && this.board.currentPlayerTurn == this.team && c.conditions()
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
				&& this.board.currentPlayerTurn == this.team;
	}

	public void printHand() {
		System.out.println("Hand " + this.team + ":");
		for (Card c : this.hand.cards) {
			System.out.print(c.id + " ");
		}
		System.out.println();
	}

	// TODO magic numbers lmao
	public boolean overflow() {
		return this.maxmana >= 7;
	}

	public boolean vengeance() {
		if (this.leader != null) {
			return this.leader.health <= 15;
		}
		return false;
	}

	public boolean resonance() {
		return this.deck.cards.size() % 2 == 0;
	}
}
