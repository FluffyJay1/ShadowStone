package server;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import client.Game;
import server.card.*;
import server.card.target.CardTargetingScheme;
import server.event.*;
import server.event.eventburst.EventBurst;
import server.event.eventgroup.EventGroup;

// must use either ClientBoard or ServerBoard
public abstract class Board {
    public Player player1, player2;
    // localteam is the team of the player, i.e. at the bottom of the screen
    private int currentPlayerTurn;
    private int localteam;
    private int winner;
    private Phase phase;

    // table of all cards
    // an index into this table uniquely identifies a card
    public List<Card> cardTable;

    // the hierarchy of groups we are under
    public List<EventGroup> eventGroups;

    public boolean mulligan;

    public Board() {
        this.init();
    }

    // reset state
    public void init() {
        this.currentPlayerTurn = 0;
        this.localteam = 0;
        this.winner = 0;
        this.player1 = new Player(this, 1);
        this.player2 = new Player(this, -1);
        this.cardTable = new ArrayList<>();
        this.eventGroups = new LinkedList<>();
        this.mulligan = true;
        this.phase = Phase.DURING_TURN;
    }

    public Board(int localteam) {
        this();
        this.localteam = localteam;
    }

    public Player getPlayer(int team) {
        return team == 1 ? player1 : player2;
    }

    /**
     * High level function to gather the cards according to a query to be run on
     * a player or potentially both.
     *
     * @param team Team to get the cards from, or 0 if from both teams
     * @param queryFunc Function to get what we want form each player
     * @return A list of the relevant cards
     */
    public <T extends Card> Stream<T> getPlayerCards(int team, Function<Player, List<T>> queryFunc) {
        if (team == 0) {
            return Stream.concat(queryFunc.apply(this.getPlayer(1)).stream(), queryFunc.apply(this.getPlayer(-1)).stream());
        } else if (team > 0) {
            return queryFunc.apply(this.getPlayer(1)).stream();
        } else {
            return queryFunc.apply(this.getPlayer(-1)).stream();
        }
    }

    // same as above but for functions that return single cards
    public <T extends Card> Stream<T> getPlayerCard(int team, Function<Player, Optional<T>> queryFunc) {
        List<T> ret = new ArrayList<>();
        if (team >= 0) {
            queryFunc.apply(this.getPlayer(1)).ifPresent(ret::add);
        }
        if (team <= 0) {
            queryFunc.apply(this.getPlayer(-1)).ifPresent(ret::add);
        }
        return ret.stream();
    }

    public Stream<Card> getTargetableCards() {
        return Stream.concat(
                this.getPlayerCards(0, Player::getPlayArea),
                Stream.concat(
                        this.getPlayerCards(0, Player::getHand),
                        this.getPlayerCard(0, Player::getLeader)
                )
        );
    }

    public Stream<Card> getTargetableCards(CardTargetingScheme t) {
        if (t == null) {
            return Stream.empty();
        }
        return this.getTargetableCards()
                .filter(t::canTarget);
    }

    /**
     * @return All the cards associated with this board, in the order they were created, as a stream.
     */
    public Stream<Card> getCards() {
        return this.cardTable.stream();
    }

    /**
     * Fetches board objects and does some common filtering on it
     *
     * @param team The team to find, or 0 if we want both teams
     * @param leader Whether to include the leader
     * @param minion Whether to incude minions
     * @param amulet Whether to include amulets
     * @param alive Whether we need our things to be not marked for death
     * @return The list of board objects that fit the criteria
     */
    public Stream<BoardObject> getBoardObjects(int team, boolean leader, boolean minion, boolean amulet, boolean alive) {
        Stream<BoardObject> stream = Stream.empty();
        if (leader) {
            stream = this.getPlayerCard(team, Player::getLeader)
                    .filter(l -> !alive || l.alive)
                    .map(l -> l); // this is necessary 100%
        }
        stream = Stream.concat(stream,
                this.getPlayerCards(team, Player::getPlayArea)
                        .filter(c -> (minion || !(c instanceof Minion)) && (amulet || !(c instanceof Amulet)) && (!alive || c.alive)));
        return stream;
    }

    /**
     * Get every card in play, in order that e.g. hooks are expected to resolve
     * @param firstTeam The first team to iterate through
     * @return All cards in play
     */
    public Stream<Card> getEverythingInPlay(int firstTeam) {
        if (firstTeam == 0) {
            firstTeam = 1;
        }
        return Stream.concat(
                Stream.concat(
                    this.getBoardObjects(firstTeam, true, true, true, false),
                    this.getPlayer(firstTeam).getUnleashPower().stream()
                ),
                Stream.concat(
                    this.getBoardObjects(firstTeam * -1, true, true, true, false),
                    this.getPlayer(firstTeam * -1).getUnleashPower().stream()
                )
        );
    }

    /**
     * Fetches minions only (excludes amulets by definition) so we don't have to
     * cast stuff ourselves, otherwise identical to getBoardObjects
     *
     * @param team The team to find, or 0 if we want both teams
     * @param leader Whether to include the leader
     * @param alive Whether we need our things to be not marked for death
     * @return The list of minions that fit the criteria
     */
    public Stream<Minion> getMinions(int team, boolean leader, boolean alive) {
        Stream<Minion> stream = Stream.empty();
        if (leader) {
            stream = this.getPlayerCard(team, Player::getLeader)
                    .filter(l -> !alive || l.alive)
                    .map(l -> l);
        }
        stream = Stream.concat(stream,
                this.getPlayerCards(team, Player::getPlayArea)
                        .filter(c -> c instanceof Minion && (!alive || c.alive))
                        .map(bo -> (Minion) bo));
        return stream;
    }

    public String stateToString() {
        StringBuilder builder = new StringBuilder();
        builder.append("State----------------------------+\n");
        builder.append("player turn: ");
        builder.append(this.getCurrentPlayerTurn());
        builder.append(", winner: ");
        builder.append(this.getWinner());
        builder.append("\n");
        this.player1.appendStringToBuilder(builder);
        builder.append("\n");
        this.player2.appendStringToBuilder(builder);
        builder.append("\n");
        this.getCards().forEachOrdered(c -> {
            c.appendStringToBuilder(builder);
            builder.append("\n");
        });
        builder.append("---------------------------------+\n");
        return builder.toString();
    }

    /**
     * Parses a set of events/eventgroups and applies their changes to the board state
     * @param s The string to parse
     */
    public void parseEventString(String s) {
        String[] lines = s.split(Game.EVENT_END);
        for (String line : lines) {
            if (!line.isEmpty()) {
                try {
                    StringTokenizer st = new StringTokenizer(line);
                    char firstChar = line.charAt(0);
                    if (firstChar == EventGroup.PUSH_TOKEN) {
                        this.pushEventGroup(EventGroup.fromString(this, st));
                    } else if (firstChar == EventGroup.POP_TOKEN) {
                        this.popEventGroup();
                    } else {
                        Event e = EventFactory.fromString(this, st);
                        this.processEvent(e);
                    }
                } catch (Exception e) {
                    System.err.println("EXCEPTION WHILE PARSING THIS EVENT");
                    System.err.println(line);
                    e.printStackTrace();
                    throw e;
                }
            }
        }
    }

    /**
     * Updates the state of the board according to the eventstrings encapsulated
     * by the event bursts.
     * @param bursts The list of event bursts to process
     */
    public void consumeEventBursts(List<EventBurst> bursts) {
        for (EventBurst eb : bursts) {
            this.parseEventString(eb.eventString);
        }
    }

    // kekl
    public <T extends Event> T processEvent(T e) {
        if (this.getWinner() != 0 || !e.conditions()) {
            return e;
        }
        e.resolve(this);
        return e;
    }

    public void pushEventGroup(EventGroup group) {
        this.eventGroups.add(group);
    }

    public EventGroup popEventGroup() {
        return this.eventGroups.remove(this.eventGroups.size() - 1);
    }

    public EventGroup peekEventGroup() {
        if (this.eventGroups.isEmpty()) {
            return null;
        }
        return this.eventGroups.get(this.eventGroups.size() - 1);
    }

    public int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public void setCurrentPlayerTurn(int currentPlayerTurn) {
        this.currentPlayerTurn = currentPlayerTurn;
    }

    public int getLocalteam() {
        return localteam;
    }

    public void setLocalteam(int localteam) {
        this.localteam = localteam;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }

    public Phase getPhase() {
        return this.phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    // for ui purposes i guess
    public static enum Phase {
        DURING_TURN,
        AFTER_TURN,
        // why is mulligan not here? idk
    }
}
