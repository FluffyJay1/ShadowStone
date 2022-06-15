package server;

import server.card.*;
import server.card.effect.*;
import utils.HistoricalList;
import utils.PositionedList;
import utils.StringBuildable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Player implements StringBuildable {
    // if we allow increasing the board size, we definitely can't go above this number
    public static final int MAX_MAX_BOARD_SIZE = 10;
    public static final int DEFAULT_MAX_HAND_SIZE = 10;
    public static final int DEFAULT_MAX_BOARD_SIZE = 6;
    public static final int OVERFLOW_THRESHOLD = 7;
    public static final int VENGEANCE_THRESHOLD = 15;
    public Player realPlayer;
    public final Board board;
    protected final PositionedList<Card> deck;
    protected final PositionedList<Card> hand;
    protected final HistoricalList<BoardObject> playArea; // things on board
    protected final PositionedList<Card> graveyard;
    protected final PositionedList<Card> banished;
    public final int team;
    public int mana;
    public int maxmana;
    public int maxmaxmana; // don't ask
    public int maxHandSize;
    public int maxPlayAreaSize;
    public boolean unleashAllowed = true;
    protected Leader leader;
    protected UnleashPower unleashPower;
    public boolean mulliganed;
    public int cardsPlayedThisTurn;
    public int shadows;

    public Player(Board board, int team) {
        this.board = board;
        this.team = team;
        this.deck = new PositionedList<>(new ArrayList<>(), c -> {
            c.status = CardStatus.DECK;
            c.visibility = CardVisibility.NONE;
            c.team = this.team;
            c.player = this;
        });
        this.hand = new PositionedList<>(new ArrayList<>(), c -> {
            c.status = CardStatus.HAND;
            c.visibility = CardVisibility.ALLIES;
            c.team = this.team;
            c.player = this;
        });
        this.playArea = new HistoricalList<>(new PositionedList<>(new ArrayList<>(), c -> {
            c.status = CardStatus.BOARD;
            c.visibility = CardVisibility.ALL;
            c.team = this.team;
            c.player = this;
        }));
        this.graveyard = new PositionedList<>(new ArrayList<>(), c -> {
            c.status = CardStatus.GRAVEYARD;
            c.visibility = CardVisibility.NONE;
            c.team = this.team;
            c.player = this;
        });
        this.banished = new PositionedList<>(new ArrayList<>(), c -> {
            c.status = CardStatus.BANISHED;
            c.visibility = CardVisibility.NONE;
            c.team = this.team;
            c.player = this;
        });
        this.mana = 0;
        this.maxmana = 3;
        this.maxmaxmana = 10;
        this.maxHandSize = DEFAULT_MAX_HAND_SIZE;
        this.maxPlayAreaSize = DEFAULT_MAX_BOARD_SIZE;
        this.mulliganed = false;
        this.cardsPlayedThisTurn = 0;
        this.shadows = 0;
    }

    public List<Card> getDeck() {
        return this.deck;
    }

    public List<Card> getHand() {
        return this.hand;
    }

    public HistoricalList<BoardObject> getPlayArea() {
        return this.playArea;
    }

    public List<Card> getGraveyard() {
        return this.graveyard;
    }

    public List<Card> getBanished() {
        return this.banished;
    }

    public Optional<Leader> getLeader() {
        return Optional.ofNullable(this.leader);
    }

    public void setLeader(Leader leader) {
        this.leader = leader;
        if (leader != null) {
            leader.setIndex(0);
            leader.status = CardStatus.LEADER;
            leader.team = this.team;
            leader.player = this;
        }
    }

    public Optional<UnleashPower> getUnleashPower() {
        return Optional.ofNullable(this.unleashPower);
    }

    public void setUnleashPower(UnleashPower up) {
        this.unleashPower = up;
        if (up != null) {
            up.setIndex(0);
            up.status = CardStatus.UNLEASHPOWER;
            up.team = this.team;
            up.player = this;
        }
    }

    public List<Card> getFromStatus(CardStatus status) {
        return switch (status) {
            case DECK -> this.getDeck();
            case HAND -> this.getHand();
            case BOARD -> new ArrayList<>(this.getPlayArea());
            case LEADER -> this.getLeader().map(l -> List.of((Card) l)).orElse(List.of());
            case UNLEASHPOWER -> this.getUnleashPower().map(up -> List.of((Card) up)).orElse(List.of());
            case GRAVEYARD -> this.getGraveyard();
            case BANISHED -> this.getBanished();
        };
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.appendStringToBuilder(builder);
        return builder.toString();
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        builder.append(this.team).append(" ").append(this.mana).append(" ").append(this.maxmana)
                .append(" ").append(this.maxmaxmana).append(" ")
                .append(this.unleashAllowed).append(" ")
                .append(this.mulliganed).append(" ")
                .append(this.cardsPlayedThisTurn).append(" ")
                .append(this.shadows).append(" ");
    }

    // uh
    public boolean canPlayCard(Card c) {
        if (c instanceof BoardObject && this.getPlayArea().size() >= this.maxPlayAreaSize) {
            return false;
        }
        return c != null && c.canBePlayed() && this.board.currentPlayerTurn == this.team
                && this.mana >= c.finalStats.get(Stat.COST) && c.status.equals(CardStatus.HAND);
    }

    public boolean canUnleashCard(Card c) {
        if (!(c instanceof Minion)) {
            return false;
        }
        Minion m = (Minion) c;
        return m.canBeUnleashed() && m.team == this.team && m.alive && this.canUnleash();
    }

    public boolean canUnleash() {
        return this.unleashAllowed && this.unleashPower != null
                && this.unleashPower.unleashesThisTurn < this.unleashPower.finalStats
                        .get(Stat.ATTACKS_PER_TURN)
                && this.mana >= this.unleashPower.finalStats.get(Stat.COST)
                && this.board.currentPlayerTurn == this.team;
    }

    // TODO magic numbers lmao
    public boolean overflow() {
        return this.maxmana >= OVERFLOW_THRESHOLD;
    }

    public boolean vengeance() {
        if (this.leader != null) {
            return this.leader.health <= VENGEANCE_THRESHOLD;
        }
        return false;
    }

    public boolean resonance() {
        return this.deck.size() % 2 == 0;
    }
}
