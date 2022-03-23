package server;

import client.Game;
import server.card.BoardObject;
import server.card.Card;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.event.Event;
import server.event.EventMulliganPhaseEnd;
import server.event.eventburst.EventBurst;
import server.event.eventgroup.EventGroup;
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

    boolean enableOutput = true;
    public boolean logEvents = true; // used by the ai to prevent history from being appended to

    // see EffectUntilTurnEnd, Card, TurnEndResolver
    public List<Effect> effectsToRemoveAtEndOfTurn;

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
        this.effectsToRemoveAtEndOfTurn = new ArrayList<>();
        this.enableOutput = true;
    }

    /**
     * Resolve a resolver, wrapping the events into an EventBurst.
     * @param r The resolver to resolve
     * @param team the team/player that triggered this resolver
     * @return Summary of resolving r
     */
    public ResolutionResult resolve(Resolver r, int team) {
        this.currentBurst.delete(0, this.currentBurst.length());
        List<Event> l = new LinkedList<>();
        ResolverQueue rq = new ResolverQueue();
        r.onResolve(this, rq, l);
        boolean rng = r.rng;
        while (!rq.isEmpty()) {
            if (this.winner != 0) {
                break;
            }
            r = rq.remove();
            r.onResolve(this, rq, l);
            if (r.rng) {
                rng = true;
            }
        }
        this.history.add(new EventBurst(team, this.currentBurst.toString()));
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
        if (this.winner != 0 || !e.conditions()) {
            return e;
        }
        String eventString = e.toString();
        e.resolve(this);
        if (el != null) {
            el.add(e);
        }
        if (!eventString.isEmpty() && e.send && this.enableOutput && this.logEvents) {
            this.currentBurst.append(eventString);
        }

        Set<EffectAura> newAuras = this.getActiveAuras().collect(Collectors.toSet());
        Set<EffectAura> removedAuras = new HashSet<>(this.lastCheckedActiveAuras);
        removedAuras.removeAll(newAuras);
        Set<EffectAura> addedAuras = new HashSet<>(newAuras);
        addedAuras.removeAll(this.lastCheckedActiveAuras);
        // make newAuras store the set of maintained auras, neither added nor removed
        Set<EffectAura> retainedAuras = new HashSet<>(newAuras);
        retainedAuras.retainAll(this.lastCheckedActiveAuras);
        for (EffectAura aura : addedAuras) {
            aura.lastCheckedAffectedCards = aura.findAffectedCards();
            if (!aura.lastCheckedAffectedCards.isEmpty()) {
                rq.add(new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Set<Card> shouldApply = aura.findAffectedCards();
                        if (!shouldApply.isEmpty()) {
                            this.resolve(b, rq, el, new AddEffectResolver(new ArrayList<>(shouldApply), aura.effectToApply));
                        }
                    }
                });
            }
        }
        for (EffectAura aura : removedAuras) {
            if (!aura.lastCheckedAffectedCards.isEmpty()) {
                rq.add(new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                        Set<Card> currentApplied = aura.currentActiveEffects.keySet();
                        if (!currentApplied.isEmpty()) {
                            List<Effect> effectsToRemove = currentApplied.stream()
                                    .map(unaffected -> aura.currentActiveEffects.get(unaffected))
                                    .collect(Collectors.toList());
                            this.resolve(b, rq, el, new RemoveEffectResolver(effectsToRemove));
                        }
                    }
                });
            }
            aura.lastCheckedAffectedCards.clear();
        }
        for (EffectAura aura : retainedAuras) {
            Set<Card> currentAffected = aura.findAffectedCards();
            if (!currentAffected.equals(aura.lastCheckedAffectedCards)) {
                // only add a resolver if something not right
                rq.add(new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
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
                });
            }
            aura.lastCheckedAffectedCards = currentAffected;
        }
        this.lastCheckedActiveAuras = newAuras;
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
        this.getCards().forEachOrdered(c -> {
            rq.addAll(c.onListenEvent(e));
        });

        return e;
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
        // we must have gotten kira queened, keep auras consistent
        this.updateAuraLastCheck();
    }

    // changing board state all willy nilly outside of the resolver can mess
    // things up with aura checking optimizations, explicity sync here
    public void updateAuraLastCheck() {
        this.lastCheckedActiveAuras = this.getActiveAuras().collect(Collectors.toSet());
        this.getCards().forEach(c -> {
            for (EffectAura aura : c.auras) {
                aura.lastCheckedAffectedCards = aura.findAffectedCards();
            }
        });
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
            }
        }, 0);
        result.concat(this.resolve(new TurnStartResolver(this.getPlayer(1)), 1));
        return result;
    }

    public ResolutionResult endCurrentPlayerTurn() {
        ResolutionResult result = this.resolve(new TurnEndResolver(this.getPlayer(this.currentPlayerTurn)), this.currentPlayerTurn);
        result.concat(this.resolve(new TurnStartResolver(this.getPlayer(this.currentPlayerTurn * -1)), this.currentPlayerTurn * -1));
        return result;
    }

}
