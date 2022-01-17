package server;

import server.card.BoardObject;
import server.card.Card;
import server.card.effect.Effect;
import server.card.effect.EffectAura;
import server.event.Event;
import server.event.eventgroup.EventGroup;
import server.playeraction.PlayerAction;
import server.resolver.*;

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

    private List<Resolver> resolveList;

    StringBuilder output, history;

    Set<EffectAura> lastCheckedActiveAuras;

    public ServerBoard(int localteam) {
        super(localteam);
    }

    @Override
    public void init() {
        super.init();
        this.resolveList = new LinkedList<>();
        this.output = new StringBuilder();
        this.history = new StringBuilder();
        this.lastCheckedActiveAuras = new HashSet<>();
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
            rl.add(new Resolver(false) {
                @Override
                public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
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
                public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
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
        for (EffectAura aura : retainedAuras) {
            Set<Card> currentAffected = aura.findAffectedCards();
            if (!currentAffected.equals(aura.lastCheckedAffectedCards)) {
                // only add a resolver if something not right
                rl.add(new Resolver(false) {
                    @Override
                    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
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
        this.lastCheckedActiveAuras = newAuras;
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

        return e;
    }

    @Override
    public synchronized void parseEventString(String s) {
        super.parseEventString(s);
        this.output.append(s);
        this.history.append(s);
        // we must have gotten kira queened, keep auras consistent
        this.updateAuraCheckLastCheck();
    }

    // changing board state all willy nilly outside of the resolver can mess
    // things up with aura checking optimizations, explicity sync here
    public void updateAuraCheckLastCheck() {
        this.lastCheckedActiveAuras = this.getActiveAuras().collect(Collectors.toSet());
        this.getCards().forEach(c -> {
            for (EffectAura aura : c.auras) {
                aura.lastCheckedAffectedCards = aura.findAffectedCards();
            }
        });
    }

    @Override
    public EventGroup popEventGroup() {
        EventGroup eg = super.popEventGroup();
        if (eg.committed) {
            this.output.append(EventGroup.POP_TOKEN + "\n");
            this.history.append(EventGroup.POP_TOKEN + "\n");
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
