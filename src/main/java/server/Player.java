package server;

import server.card.*;
import server.card.effect.*;
import server.card.unleashpower.*;

public class Player {
    public Player realPlayer;
    public final Board board;
    public final Deck deck;
    public final Hand hand;
    public final int team;
    public int mana = 0;
    public int maxmana = 3;
    public int maxmaxmana = 10; // don't ask
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
        return this.team + " " + this.mana + " " + this.maxmana + " " +
                this.maxmaxmana + " " + this.unleashAllowed + " ";
    }

    // uh
    public boolean canPlayCard(Card c) {
        return c != null && this.board.currentPlayerTurn == this.team && c.conditions()
                && this.mana >= c.finalStatEffects.getStat(EffectStats.COST) && c.status.equals(CardStatus.HAND);
    }

    public boolean canUnleashCard(Card c) {
        return c instanceof Minion && ((Minion) c).canBeUnleashed() && c.team == this.team && this.unleashConditions();
    }

    public boolean canUnleash() {
        if (!unleashConditions()) {
            return false;
        }
        for (Minion m : this.board.getMinions(this.team, false, true)) {
            if (m.canBeUnleashed()) {
                return true;
            }
        }
        return false;
    }

    private boolean unleashConditions() {
        return this.unleashAllowed
                && this.unleashPower.unleashesThisTurn < this.unleashPower.finalStatEffects
                        .getStat(EffectStats.ATTACKS_PER_TURN)
                && this.mana >= this.unleashPower.finalStatEffects.getStat(EffectStats.COST)
                && this.board.currentPlayerTurn == this.team;
    }

    public void printHand() {
        System.out.println("Hand " + this.team + ":");
        for (Card c : this.hand.cards) {
            System.out.print(c.getClass().getName() + " ");
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
