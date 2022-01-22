package server;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import client.Game;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.resolver.Resolver;

// must use either ClientBoard or ServerBoard
public abstract class Board {
    public Player player1, player2;
    // localteam is the team of the player, i.e. at the bottom of the screen
    public int currentPlayerTurn, localteam, winner;

    // the hierarchy of groups we are under
    public List<EventGroup> eventGroups;

    public Board() {
        this.init();
    }

    // reset state
    public void init() {
        this.currentPlayerTurn = 1;
        this.localteam = 1;
        this.winner = 0;
        this.player1 = new Player(this, 1);
        this.player2 = new Player(this, -1);
        this.eventGroups = new LinkedList<>();
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

    // cards that can be added to a Target object
    public Stream<Card> getTargetableCards(Target t) {
        if (t == null) {
            return Stream.empty();
        }
        return this.getTargetableCards().filter(t::canTarget);
    }

    public Stream<Card> getCards() {
        // love me some stream concatenation
        return Stream.concat(
                Stream.concat(
                        Stream.concat(
                                this.getPlayerCards(0, Player::getPlayArea),
                                this.getPlayerCards(0, Player::getHand)
                        ),
                        Stream.concat(
                                this.getPlayerCards(0, Player::getDeck),
                                this.getPlayerCards(0, Player::getGraveyard)
                        )
                ),
                Stream.concat(
                        Stream.concat(
                                this.getPlayerCards(0, Player::getBanished),
                                this.getPlayerCard(0, Player::getUnleashPower)
                        ),
                        this.getPlayerCard(0, Player::getLeader)
                )
        );
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

    public Stream<EffectAura> getActiveAuras() {
        return this.getBoardObjects(0, true, true, true, false)
                .flatMap(bo -> bo.auras.stream())
                .filter(aura -> !aura.mute);
    }

    public String stateToString() {
        StringBuilder builder = new StringBuilder();
        builder.append("State----------------------------+\n");
        builder.append("player turn: ");
        builder.append(this.currentPlayerTurn);
        builder.append(", winner: ");
        builder.append(this.winner);
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
    public synchronized void parseEventString(String s) {
        if (!s.isEmpty()) {
            String[] lines = s.split(Game.EVENT_END);
            for (String line : lines) {
                StringTokenizer st = new StringTokenizer(line);
                char firstChar = line.charAt(0);
                if (firstChar == EventGroup.PUSH_TOKEN) {
                    this.pushEventGroup(EventGroup.fromString(this, st));
                } else if (firstChar == EventGroup.POP_TOKEN) {
                    this.popEventGroup();
                } else {
                    Event e = Event.createFromString(this, st);
                    this.processEvent(e);
                }
            }
        }
    }

    // kekl
    public <T extends Event> T processEvent(T e) {
        if (this.winner != 0 || !e.conditions()) {
            return e;
        }
        e.resolve();
        return e;
    }

    public void pushEventGroup(EventGroup group) {
        this.eventGroups.add(group);
        // don't commit to output just yet, we don't know if this group is empty or not
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

}
