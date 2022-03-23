package server;

import client.Game;
import server.card.BoardObject;
import server.card.Card;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.event.Event;
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
    StringBuilder output, history;

    Set<EffectAura> lastCheckedActiveAuras;

    boolean enableOutput = true;

    // see EffectUntilTurnEnd, Card, TurnEndResolver
    public List<Effect> effectsToRemoveAtEndOfTurn;

    public ServerBoard(int localteam) {
        super(localteam);
    }

    @Override
    public void init() {
        super.init();
        this.output = new StringBuilder();
        this.history = new StringBuilder();
        this.lastCheckedActiveAuras = new HashSet<>();
        this.effectsToRemoveAtEndOfTurn = new ArrayList<>();
        this.enableOutput = true;
    }

    // quality helper methods
    public ResolutionResult resolve(Resolver r) {
        List<Event> l = new LinkedList<>();
        ResolverQueue rq = new ResolverQueue();
        r.onResolve(this, rq, l);
        ResolutionResult result = new ResolutionResult(l, r.rng);
        result.concat(this.resolveAll(rq));
        return result;
    }

    public ResolutionResult resolveAll(ResolverQueue resolveQueue) {
        List<Event> l = new LinkedList<>();
        ResolutionResult result = new ResolutionResult(l, false);
        while (!resolveQueue.isEmpty()) {
            if (this.winner != 0) {
                break;
            }
            Resolver r = resolveQueue.remove();
            r.onResolve(this, resolveQueue, l);
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
    public <T extends Event> T processEvent(ResolverQueue rq, List<Event> el, T e) {
        if (this.winner != 0 || !e.conditions()) {
            return e;
        }
        String eventString = e.toString();
        e.resolve(this);
        if (el != null) {
            el.add(e);
        }
        // TODO make the AI board not do this
        if (!eventString.isEmpty() && e.send && this.enableOutput) {
            this.output.append(eventString);
            this.history.append(eventString);
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

    @Override
    public synchronized void parseEventString(String s) {
        this.enableOutput = false;
        super.parseEventString(s);
        this.enableOutput = true;
        this.output.append(s);
        this.history.append(s);
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
        if (this.enableOutput) {
            this.output.append(group.toString());
            this.history.append(group.toString());
        }
    }

    @Override
    public EventGroup popEventGroup() {
        EventGroup eg = super.popEventGroup();
        if (this.enableOutput) {
            this.output.append(EventGroup.POP_TOKEN + Game.EVENT_END);
            this.history.append(EventGroup.POP_TOKEN + Game.EVENT_END);
        }
        return eg;
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
        try {
            PrintWriter pw = new PrintWriter("board.dat", StandardCharsets.UTF_16);
            PrintWriter pwreadable = new PrintWriter("board_readable.txt", StandardCharsets.UTF_16);
            pw.print(this.getHistory());
            pwreadable.print(this.getHistory().replaceAll(Game.EVENT_END, "\n"));
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
                this.parseEventString(state);
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

    public ResolutionResult endCurrentPlayerTurn() {
        ResolutionResult result = this.resolve(new TurnEndResolver(this.getPlayer(this.currentPlayerTurn)));
        result.concat(this.resolve(new TurnStartResolver(this.getPlayer(this.currentPlayerTurn * -1))));
        return result;
    }

}
