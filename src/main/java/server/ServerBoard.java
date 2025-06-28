package server;

import client.Game;
import server.card.BoardObject;
import server.card.Card;
import server.card.CardStatus;
import server.card.cardset.basic.neutral.NotCoin;
import server.card.effect.*;
import server.event.Event;
import server.event.EventMulliganPhaseEnd;
import server.event.eventburst.EventBurst;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.playeraction.PlayerAction;
import server.resolver.*;
import server.resolver.util.ResolverQueue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Variant of the Board class that handles proper event/listener/aura resolution
 * via Resolvers. Serves as the single source of truth for a board state; its
 * mirrors are ClientBoards.
 */
public class ServerBoard extends Board {
    List<EventBurst> history;
    int outputStart;
    StringBuilder currentBurst;

    Set<EffectAura> lastCheckedActiveAuras;
    Set<EffectWithDependentStats> lastCheckedActiveDependentStats;

    boolean enableOutput = true;
    public boolean logEvents = true; // used by the ai to prevent history from being appended to

    // The following are subsets of all effects, and are updated in Card's addEffect and removeEffect
    // however for basic effects created upon card creation, we must rely on EventCreateCard's undo
    // for convenience, a subset of all effects that are also auras
    public Set<EffectAura> auras;
    // like above but for effects with dependent stats
    public Set<EffectWithDependentStats> dependentStats;
    // same but for cards that have eventlisteners effects, may be muted tho (also for optimization)
    public Set<Card> listeners;

    // see Card, TurnEndResolver
    public List<Effect> effectsToRemoveAtEndOfTurn;

    // see PerTurnCounter
    public Set<Effect> effectsWithPerTurnCounters;

    boolean updateCheckersResolverAdded;

    public ServerBoard(int localteam) {
        super(localteam);
    }

    @Override
    public void init() {
        super.init();
        this.history = new ArrayList<>();
        this.outputStart = 0;
        this.currentBurst = new StringBuilder();
        this.lastCheckedActiveAuras = new HashSet<>();
        this.lastCheckedActiveDependentStats = new HashSet<>();
        this.auras = new HashSet<>();
        this.dependentStats = new HashSet<>();
        this.listeners = new TreeSet<>(Comparator.comparing(Card::getRef));
        this.effectsToRemoveAtEndOfTurn = new ArrayList<>();
        this.effectsWithPerTurnCounters = new HashSet<>();
        this.updateCheckersResolverAdded = false;

        this.enableOutput = true;
    }

    // Whenever a new effect gets added, do some preprocessing to optimize lookup
    public void registerNewEffect(Effect e) {
        // because these types of effects are rare but need to be checked frequently,
        // register these to the ServerBoard to optimize lookup
        if (e instanceof EffectAura) {
            this.auras.add((EffectAura) e);
        }
        if (e instanceof EffectWithDependentStats) {
            this.dependentStats.add((EffectWithDependentStats) e);
        }
        if (e.untilTurnEndTeam != null) {
            this.effectsToRemoveAtEndOfTurn.add(e);
        }
        if (e.onListenEvent(null) != Effect.UNIMPLEMENTED_RESOLVER) {
            e.owner.listeners.add(e);
            this.listeners.add(e.owner);
        }
        if (e.onListenEventWhileInPlay(null) != Effect.UNIMPLEMENTED_RESOLVER) {
            e.owner.whileInPlayListeners.add(e);
        }
        if (e.onListenStateChangeWhileInPlay(null, null) != Effect.UNIMPLEMENTED_RESOLVER) {
            e.owner.whileInPlayStateTrackers.add(e);
        }
    }

    // opposite of registerNewEffect, when we no longer need to lookup something
    public void unregisterEffect(Effect e) {
        if (e instanceof EffectAura) {
            this.auras.remove((EffectAura) e);
        }
        if (e instanceof EffectWithDependentStats) {
            this.dependentStats.remove((EffectWithDependentStats) e);
        }
        if (e.untilTurnEndTeam != null) {
            this.effectsToRemoveAtEndOfTurn.remove(e);
        }
        if (e.onListenEvent(null) != Effect.UNIMPLEMENTED_RESOLVER) {
            e.owner.listeners.remove(e);
            if (e.owner.listeners.isEmpty()) {
                this.listeners.remove(e.owner);
            }
        }
        if (e.onListenEventWhileInPlay(null) != Effect.UNIMPLEMENTED_RESOLVER) {
            e.owner.whileInPlayListeners.remove(e);
        }
        if (e.onListenStateChangeWhileInPlay(null, null) != Effect.UNIMPLEMENTED_RESOLVER) {
            e.owner.whileInPlayStateTrackers.remove(e);
        }
    }

    public Stream<EffectAura> getActiveAuras() {
        return this.auras.stream()
                .filter(aura -> !aura.mute && aura.owner.isInPlay());
    }

    public Stream<EffectWithDependentStats> getActiveDependentStats() {
        return this.dependentStats.stream()
                .filter(EffectWithDependentStats::isActive)
                .filter(e -> !e.mute);
    }

    /**
     * Resolve a resolver, wrapping the events into an EventBurst.
     * @param r The resolver to resolve
     * @param team the team/player that triggered this resolver
     * @return Summary of resolving r
     */
    public ResolutionResult resolve(Resolver r, int team) {
        if (this.getWinner() != 0) {
            return new ResolutionResult();
        }
        this.currentBurst.delete(0, this.currentBurst.length());
        List<Event> l = new LinkedList<>();
        ResolverQueue rq = new ResolverQueue();
        r.onResolve(this, rq, l);
        boolean rng = r.rng;
        while (!rq.isEmpty()) {
            if (this.getWinner() != 0) {
                break;
            }
            r = rq.remove();
            r.onResolve(this, rq, l);
            if (r.rng) {
                rng = true;
            }
        }
        if (this.logEvents) {
            this.history.add(new EventBurst(team, this.currentBurst.toString()));
        }
        return new ResolutionResult(l, rng);
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
    public <T extends Event> T processEvent(ResolverQueue rq, List<Event> el, T e) {
        if (this.getWinner() != 0 || !e.conditions()) {
            return e;
        }
        List<Card> cardsToPerformStateTrackingFor = this.getEverythingInPlay(this.getCurrentPlayerTurn()).toList();
        cardsToPerformStateTrackingFor.forEach(Card::saveStateTrackers);
        String eventString = e.toString();
        e.resolve(this);
        if (el != null) {
            el.add(e);
        }
        if (!eventString.isEmpty() && e.send && this.enableOutput && this.logEvents) {
            this.currentBurst.append(eventString);
        }

        cardsToPerformStateTrackingFor.forEach(c -> { if (c.isInPlay()) rq.addAll(c.onListenStateChangeWhileInPlay()); });
        this.updateCheckers(rq);

        if (e.cardsEnteringPlay() != null) {
            for (BoardObject bo : e.cardsEnteringPlay()) {
                rq.addAll(bo.onEnterPlay());
            }
        }
        if (e.cardsLeavingPlay() != null) {
            for (BoardObject bo : e.cardsLeavingPlay()) {
                rq.addAll(bo.onLeavePlay());
            }
        }
        this.getEverythingInPlay(this.getCurrentPlayerTurn()).forEach(c -> rq.addAll(c.onListenEventWhileInPlay(e)));
        for (Card c : this.listeners) {
            rq.addAll(c.onListenEvent(e));
        }
        return e;
    }

    private void updateCheckers(ResolverQueue rq) {
        if (!this.updateCheckersResolverAdded) {
            rq.add(new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                    b.pushEventGroup(new EventGroup(EventGroupType.CONCURRENTDAMAGE));
                    // AURAS
                    Set<EffectAura> currentAuras = getActiveAuras().collect(Collectors.toSet());
                    Set<EffectAura> removedAuras = new HashSet<>(lastCheckedActiveAuras);
                    removedAuras.removeAll(currentAuras);
                    lastCheckedActiveAuras = currentAuras;
                    for (EffectAura aura : removedAuras) {
                        Set<Card> currentApplied = aura.currentActiveEffects.keySet();
                        if (!currentApplied.isEmpty()) {
                            List<Effect> effectsToRemove = currentApplied.stream()
                                    .map(unaffected -> aura.currentActiveEffects.get(unaffected))
                                    .collect(Collectors.toList());
                            this.resolve(b, rq, el, new RemoveEffectResolver(effectsToRemove));
                        }
                    }
                    for (EffectAura aura : currentAuras) {
                        // obtain set difference
                        Set<Card> currentApplied = aura.currentActiveEffects.keySet();
                        Set<Card> shouldApply = aura.findAffectedCards();
                        Set<Card> newAffected = new HashSet<>(shouldApply);
                        newAffected.removeAll(currentApplied);
                        if (!newAffected.isEmpty()) {
                            this.resolve(b, rq, el, new AddEffectResolver(new ArrayList<>(newAffected), aura.effectToApply));
                        }
                        Set<Card> newUnaffected = new HashSet<>(currentApplied);
                        newUnaffected.removeAll(shouldApply);
                        if (!newUnaffected.isEmpty()) {
                            List<Effect> effectsToRemove = newUnaffected.stream()
                                    .map(unaffected -> aura.currentActiveEffects.get(unaffected))
                                    .collect(Collectors.toList());
                            this.resolve(b, rq, el, new RemoveEffectResolver(effectsToRemove));
                        }
                    }
                    // DEPENDENT STATS
                    Set<EffectWithDependentStats> currentDependents = getActiveDependentStats().collect(Collectors.toSet());
                    Set<EffectWithDependentStats> currentAndPreviousDependents = new HashSet<>(lastCheckedActiveDependentStats);
                    currentAndPreviousDependents.addAll(currentDependents);
                    lastCheckedActiveDependentStats = currentDependents;
                    if (!currentAndPreviousDependents.isEmpty()) {
                        List<EffectWithDependentStats> eff = new ArrayList<>(currentAndPreviousDependents.size());
                        List<EffectStats> stats = new ArrayList<>(currentAndPreviousDependents.size());
                        for (EffectWithDependentStats effToUpdate : currentAndPreviousDependents) {
                            EffectStats calculated = effToUpdate.isActive() && !effToUpdate.mute ? effToUpdate.calculateStats() : effToUpdate.baselineStats;
                            if (!effToUpdate.effectStats.equals(calculated)) {
                                eff.add(effToUpdate);
                                stats.add(calculated);
                            }
                        }
                        this.resolve(b, rq, el, new SetEffectStatsResolver(eff, stats));
                    }
                    b.popEventGroup();
                    updateCheckersResolverAdded = false;
                }
            });
            this.updateCheckersResolverAdded = true;
       }
    }

    /**
     * Parses a set of events/eventgroups and applies their changes to the board
     * state. Does not update the board history or auras checks.
     * @param s The string to parse
     */
    @Override
    public void parseEventString(String s) {
        this.enableOutput = false;
        super.parseEventString(s);
        this.enableOutput = true;
    }

    /**
     * Updates the state and the history of the board according to the
     * eventstrings encapsulated by the event bursts. Suited for restoring the
     * state of a board after a reset.
     * @param bursts The list of event bursts to process
     */
    @Override
    public void consumeEventBursts(List<EventBurst> bursts) {
        for (EventBurst eb : bursts) {
            this.parseEventString(eb.eventString);
        }
        if (this.enableOutput && this.logEvents) {
            this.history.addAll(bursts);
        }
    }

    @Override
    public void pushEventGroup(EventGroup group) {
        super.pushEventGroup(group);
        if (this.enableOutput && this.logEvents) {
            this.currentBurst.append(group.toString());
        }
    }

    @Override
    public EventGroup popEventGroup() {
        EventGroup eg = super.popEventGroup();
        if (this.enableOutput && this.logEvents) {
            this.currentBurst.append(EventGroup.POP_TOKEN + Game.EVENT_END);
        }
        return eg;
    }

    public String retrieveEventBurstString() {
        StringBuilder ret = new StringBuilder();
        ListIterator<EventBurst> iter = this.history.listIterator(this.outputStart);
        while (iter.hasNext()) {
            ret.append(iter.next().toString());
        }
        this.outputStart = this.history.size();
        return ret.toString();
    }

    public String getHistory() {
        StringBuilder ret = new StringBuilder();
        for (EventBurst eventBurst : this.history) {
            ret.append(eventBurst.toString());
        }
        return ret.toString();
    }

    public String getReadableHistory() {
        StringBuilder ret = new StringBuilder();
        for (EventBurst eventBurst : this.history) {
            ret.append("Burst ").append(eventBurst.team)
                    .append(":\n").append(eventBurst.eventString.replaceAll(Game.EVENT_END, "\n"))
                    .append("\n");
        }
        return ret.toString();
    }

    public void saveBoardState() {
        try {
            PrintWriter pw = new PrintWriter("board.dat", StandardCharsets.UTF_16);
            PrintWriter pwreadable = new PrintWriter("board_readable.txt", StandardCharsets.UTF_16);
            pw.print(this.getHistory());
            pwreadable.print(this.getReadableHistory());
            pw.close();
            pwreadable.close();
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
                this.consumeEventBursts(EventBurst.parseEventBursts(state));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public ResolutionResult executePlayerAction(StringTokenizer st) {
        PlayerAction pa = PlayerAction.createFromString(this, st);
        return pa.perform(this);
    }

    // this is done in its own function and not in the mulligan resolver because
    // we want to separate the mulligan and the turn start bursts
    public ResolutionResult endMulliganPhase() {
        ResolutionResult result = this.resolve(new Resolver(false) {
            @Override
            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                b.processEvent(rq, el, new EventMulliganPhaseEnd());
                this.resolve(b, rq, el, CreateCardResolver.builder()
                        .withCard(new NotCoin())
                        .withTeam(-1)
                        .withStatus(CardStatus.HAND)
                        .withPos(-1)
                        .build());
            }
        }, 0);
        result.concat(this.resolve(new TurnStartResolver(this.getPlayer(1)), 1));
        return result;
    }

    public ResolutionResult endCurrentPlayerTurn() {
        ResolutionResult result = this.resolve(new TurnEndResolver(this.getPlayer(this.getCurrentPlayerTurn())), this.getCurrentPlayerTurn());
        result.concat(this.resolve(new TurnStartResolver(this.getPlayer(this.getCurrentPlayerTurn() * -1)), this.getCurrentPlayerTurn() * -1));
        return result;
    }

}
