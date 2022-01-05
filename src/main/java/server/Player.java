package server;

import server.card.*;
import server.card.effect.*;
import server.card.unleashpower.*;
import utils.PositionedList;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public static final int DEFAULT_MAX_HAND_SIZE = 10;
    public Player realPlayer;
    public final Board board;
    protected final PositionedList<Card> deck;
    protected final PositionedList<Card> hand;
    protected final PositionedList<BoardObject> playArea; // things on board
    protected final PositionedList<Card> graveyard;
    public final int team;
    public int mana;
    public int maxmana;
    public int maxmaxmana; // don't ask
    public int maxHandSize;
    public boolean unleashAllowed = true;
    protected Leader leader;
    protected UnleashPower unleashPower;

    public Player(Board board, int team) {
        this.board = board;
        this.team = team;
        this.deck = new PositionedList<>(new ArrayList<>());
        this.hand = new PositionedList<>(new ArrayList<>());
        this.playArea = new PositionedList<>(new ArrayList<>());
        this.graveyard = new PositionedList<>(new ArrayList<>());
        this.mana = 0;
        this.maxmana = 3;
        this.maxmaxmana = 10;
        this.maxHandSize = DEFAULT_MAX_HAND_SIZE;
    }

    public List<Card> getDeck() {
        return this.deck;
    }

    public List<Card> getHand() {
        return this.hand;
    }

    public List<BoardObject> getPlayArea() {
        return this.playArea;
    }

    public List<Card> getGraveyard() {
        return this.graveyard;
    }

    public Leader getLeader() {
        return this.leader;
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
        if (leader != null) {
            leader.setIndex(0);
        }
    }

    public UnleashPower getUnleashPower() {
        return this.unleashPower;
    }

    public void setUnleashPower(UnleashPower up) {
        this.unleashPower = up;
        if (up != null) {
            up.setIndex(0);
        }
    }

    public List<Card> getFromStatus(CardStatus status) {
        return switch (status) {
            case DECK -> this.getDeck();
            case HAND -> this.getHand();
            case BOARD -> new ArrayList<>(this.getPlayArea());
            case LEADER -> List.of(this.getLeader());
            case UNLEASHPOWER -> List.of(this.getUnleashPower());
            case GRAVEYARD -> this.getGraveyard();
        };
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
        return this.unleashAllowed && this.unleashPower != null
                && this.unleashPower.unleashesThisTurn < this.unleashPower.finalStatEffects
                        .getStat(EffectStats.ATTACKS_PER_TURN)
                && this.mana >= this.unleashPower.finalStatEffects.getStat(EffectStats.COST)
                && this.board.currentPlayerTurn == this.team;
    }

    public void printHand() {
        System.out.println("Hand " + this.team + ":");
        for (Card c : this.hand) {
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
        return this.deck.size() % 2 == 0;
    }
}
