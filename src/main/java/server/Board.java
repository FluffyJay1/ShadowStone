package server;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.playeraction.*;
import server.resolver.*;

public class Board {
    public boolean isServer = true; // true means it is the center of game logic
    public boolean isClient; // true means has a visualboard
    // links cards created between board and visualboard
    public List<Card> cardsCreated;

    public Player player1, player2;
    // localteam is the team of the player, i.e. at the bottom of the screen
    public int currentPlayerTurn, localteam, winner;

    private List<Resolver> resolveList;

    // the hierarchy of groups we are under
    private List<EventGroup> eventGroups;

    StringBuilder output, history;

    public Board() {
        this.init();
    }

    // reset state
    public void init() {
        this.currentPlayerTurn = 1;
        this.localteam = 1;
        this.winner = 0;
        this.cardsCreated = new LinkedList<>();
        this.player1 = new Player(this, 1);
        this.player2 = new Player(this, -1);
        this.resolveList = new LinkedList<>();
        this.output = new StringBuilder();
        this.history = new StringBuilder();
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
    public <T extends Card> Stream<T> getPlayerCard(int team, Function<Player, T> queryFunc) {
        List<T> ret = new ArrayList<>();
        if (team >= 0) {
            T addition = queryFunc.apply(this.getPlayer(1));
            if (addition != null) {
                ret.add(addition);
            }
        }
        if (team <= 0) {
            T addition = queryFunc.apply(this.getPlayer(-1));
            if (addition != null) {
                ret.add(addition);
            }
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
                .flatMap(bo -> bo.auras.stream());
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

    // quality helper methods
    public ResolutionResult resolve(Resolver r) {
        List<Event> l = new LinkedList<>();
        r.onResolve(this, this.resolveList, l);
        ResolutionResult result = new ResolutionResult(l, r.rng);
        result.concat(this.resolveAll());
        return result;
    }

    public ResolutionResult resolveAll() {
        return this.resolveAll(this.resolveList);
    }

    // Only used by server, i.e. isServer == true
    public ResolutionResult resolveAll(List<Resolver> resolveList) {
        List<Event> l = new LinkedList<>();
        ResolutionResult result = new ResolutionResult(l, false);
        while (!resolveList.isEmpty()) {
            if (this.winner != 0) {
                break;
            }
            Resolver r = resolveList.remove(0);
            r.onResolve(this, resolveList, l);
            if (r.rng) {
                result.rng = true;
            }
        }
        return result;
    }

    /*
     * Process an event, resolving it, alerting listeners, and registering it in the
     * board's history of events
     * 
     * rl is the resolver list to enqueue listener responses to
     * 
     * el is a parameter for a cheeky space-saving measure
     * 
     * in summary don't add the return value to el, the method does it for you
     * 
     */
    public <T extends Event> T processEvent(List<Resolver> rl, List<Event> el, T e) {
        if (this.winner != 0 || !e.conditions()) {
            return e;
        }
        for (EventGroup eg : this.eventGroups) {
            if (!eg.committed) {
                // commit this event group to output if it isn't empty
                this.output.append(eg.toString());
                this.history.append(eg.toString());
                eg.committed = true;
            }
        }
        Set<EffectAura> oldAuras = null;
        if (rl != null) {
            oldAuras = this.getActiveAuras().collect(Collectors.toSet());
        }
        String eventString = e.toString();
        e.resolve();
        if (el != null) {
            el.add(e);
        }
        // TODO make the AI board not do this
        if (!eventString.isEmpty() && e.send) {
            this.output.append(eventString);
            this.history.append(eventString);
        }
        if (rl != null) {
            Set<EffectAura> newAuras = this.getActiveAuras().collect(Collectors.toSet());
            Set<EffectAura> removedAuras = new HashSet<>(oldAuras);
            removedAuras.removeAll(newAuras);
            Set<EffectAura> addedAuras = new HashSet<>(newAuras);
            addedAuras.removeAll(oldAuras);
            // make newAuras store the set of maintained auras, neither added nor removed
            newAuras.retainAll(oldAuras);
            for (EffectAura aura : addedAuras) {
                aura.lastCheckedAffectedCards = aura.findAffectedCards();
                rl.add(new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        Set<Card> shouldApply = aura.findAffectedCards();
                        if (!shouldApply.isEmpty()) {
                            this.resolve(b, rl, el, new AddEffectResolver(new ArrayList<>(shouldApply), aura.effectToApply));
                        }
                    }
                });
            }
            for (EffectAura aura : removedAuras) {
                aura.lastCheckedAffectedCards.clear();
                rl.add(new Resolver(false) {
                    @Override
                    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                        Set<Card> currentApplied = aura.currentActiveEffects.keySet();
                        if (!currentApplied.isEmpty()) {
                            List<Effect> effectsToRemove = currentApplied.stream()
                                    .map(unaffected -> aura.currentActiveEffects.get(unaffected))
                                    .collect(Collectors.toList());
                            this.resolve(b, rl, el, new RemoveEffectResolver(effectsToRemove));
                        }
                    }
                });
            }
            for (EffectAura aura : newAuras) {
                Set<Card> currentAffected = aura.findAffectedCards();
                if (!currentAffected.equals(aura.lastCheckedAffectedCards)) {
                    // only add a resolver if something not right
                    rl.add(new Resolver(false) {
                        @Override
                        public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                            // obtain set difference
                            Set<Card> currentApplied = aura.currentActiveEffects.keySet();
                            Set<Card> shouldApply = aura.findAffectedCards();
                            Set<Card> newAffected = new HashSet<>(shouldApply);
                            newAffected.removeAll(currentApplied);
                            if (!newAffected.isEmpty()) {
                                this.resolve(b, rl, el, new AddEffectResolver(new ArrayList<>(newAffected), aura.effectToApply));
                            }
                            Set<Card> newUnaffected = new HashSet<>(currentApplied);
                            newUnaffected.removeAll(shouldApply);
                            if (!newUnaffected.isEmpty()) {
                                List<Effect> effectsToRemove = newUnaffected.stream()
                                        .map(unaffected -> aura.currentActiveEffects.get(unaffected))
                                        .collect(Collectors.toList());
                                this.resolve(b, rl, el, new RemoveEffectResolver(effectsToRemove));
                            }
                        }
                    });
                }
                aura.lastCheckedAffectedCards = currentAffected;
            }
            if (e.cardsEnteringPlay() != null) {
                for (BoardObject bo : e.cardsEnteringPlay()) {
                    rl.add(new FlagResolver(bo, bo.getResolvers(Effect::onEnterPlay)));
                }
            }
            if (e.cardsLeavingPlay() != null) {
                for (BoardObject bo : e.cardsLeavingPlay()) {
                    rl.addAll(bo.getResolvers(Effect::onLeavePlay));
                }
            }
            this.getCards().forEachOrdered(c -> {
                List<Resolver> listenEventResolvers = new LinkedList<>();
                for (Effect listener : c.listeners) {
                    listenEventResolvers.add(listener.onListenEvent(e));
                }
                rl.add(new FlagResolver(c, listenEventResolvers));
            });
        } else if (this.isServer) {
            // rl is null and we are running on the server, we must have gotten kira queened
            this.getCards().forEach(c -> {
                for (EffectAura aura : c.auras) {
                   aura.lastCheckedAffectedCards = aura.findAffectedCards();
                }
            });
        }
        return e;
    }

    public String retrieveEventString() {
        String temp = this.output.toString();
        this.output.delete(0, this.output.length());
        return temp;
    }

    public String getHistory() {
        return this.history.toString();
    }

    public void saveBoardState() {
        File f = new File("board.dat");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            PrintWriter pw = new PrintWriter(f, StandardCharsets.UTF_16);
            pw.print(this.getHistory());
            pw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void loadBoardState() {
        File f = new File("board.dat");
        if (f.exists()) {
            try {
                this.init();
                String state = Files.readString(f.toPath(), StandardCharsets.UTF_16);
                this.parseEventString(state);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            try {
                f.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    
    /**
     * Parses a set of events/eventgroups and applies their changes to the board state
     * @param s The string to parse
     */
    public synchronized void parseEventString(String s) {
        if (!s.isEmpty()) {
            String[] lines = s.split("\n");
            for (String line : lines) {
                StringTokenizer st = new StringTokenizer(line);
                char firstChar = line.charAt(0);
                if (firstChar == EventGroup.PUSH_TOKEN) {
                    this.pushEventGroup(EventGroup.fromString(this, st));
                } else if (firstChar == EventGroup.POP_TOKEN) {
                    this.popEventGroup();
                } else {
                    Event e = Event.createFromString(this, st);
                    this.processEvent(null, null, e);
                }
            }
        }
    }

    public void pushEventGroup(EventGroup group) {
        this.eventGroups.add(group);
        // don't commit to output just yet, we don't know if this group is empty or not
    }

    public EventGroup popEventGroup() {
        EventGroup eg = this.eventGroups.remove(this.eventGroups.size() - 1);
        if (eg.committed) {
            this.output.append(EventGroup.POP_TOKEN + "\n");
            this.history.append(EventGroup.POP_TOKEN + "\n");
        }
        return eg;
    }

    public EventGroup peekEventGroup() {
        if (this.eventGroups.isEmpty()) {
            return null;
        }
        return this.eventGroups.get(this.eventGroups.size() - 1);
    }

    public ResolutionResult executePlayerAction(StringTokenizer st) {
        PlayerAction pa = PlayerAction.createFromString(this, st);
        return pa.perform(this);
    }

    public ResolutionResult endCurrentPlayerTurn() {
        ResolutionResult result = this.resolve(new TurnEndResolver(this.getPlayer(this.currentPlayerTurn)));
        result.concat(this.resolve(new TurnStartResolver(this.getPlayer(this.currentPlayerTurn * -1))));
        return result;
    }
}
