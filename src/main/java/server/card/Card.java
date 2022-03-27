package server.card;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import client.tooltip.*;
import client.ui.game.*;
import server.*;
import server.card.effect.*;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.Event;
import server.event.eventgroup.EventGroupType;
import server.resolver.meta.HookResolver;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.Indexable;
import utils.PositionedList;
import utils.StringBuildable;

public abstract class Card implements Indexable, StringBuildable {
    // getValue may depend on the value of other cards, put a limit to how many
    // other cards it needs to calculate
    private static final int VALUE_MAX_REF_DEPTH = 5;
    public final Board board;
    public Player player; // functional dependency with team but who cares
    public boolean alive = true; // alive means not marked for death
    public int team;
    private int cardpos;
    public int spellboosts;
    public final CardText cardText;
    private final TooltipCard tooltip;
    public CardStatus status;
    public Card realCard; // for visual board
    public Card visualCard; // for client board
    public UICard uiCard;

    public EffectStats.StatSet finalStatEffects = new EffectStats.StatSet(), finalBasicStatEffects = new EffectStats.StatSet();
    /*
     * basic effects can't get removed unlike additional effects (e.g. bounce
     * effects), but they can be muted
     */
    private final PositionedList<Effect> effects = new PositionedList<>(new ArrayList<>(), e -> e.basic = false),
            basicEffects = new PositionedList<>(new ArrayList<>(), e -> e.basic = true),
            removedEffects = new PositionedList<>(new ArrayList<>(), e -> e.removed = true);

    private final Set<Effect> listeners = new HashSet<>();

    public Card(Board board, CardText cardText) {
        this.board = board;
        this.cardText = cardText;
        this.tooltip = cardText.getTooltip();
        for (Effect e : cardText.getEffects()) {
            this.addEffect(true, e);
        }
        this.status = CardStatus.DECK;
        this.spellboosts = 0;
    }

    // if card can be seen on the board
    public boolean isVisible() {
        return switch (this.status) {
            case HAND, BOARD, LEADER, UNLEASHPOWER -> true;
            default -> false;
        };
    }

    // overrided
    public boolean isInPlay() {
        return false;
    }

    /**
     * Estimates a "power level" of a card, for an AI to use to evaluate board
     * state. Values should be in terms of equivalent mana worth.
     *
     * @return the approximate mana worth of the card
     */
    public final double getValue() {
        return this.getValue(VALUE_MAX_REF_DEPTH);
    }

    public abstract double getValue(int refs);

    public double getTotalEffectValueOf(Function<Effect, Double> property) {
        // functional is cool
        return this.getFinalEffects(true)
                .map(property)
                .reduce(0., Double::sum);
    }

    public List<Effect> getEffects(boolean basic) {
        return basic ? this.basicEffects : this.effects;
    }

    public List<Effect> getRemovedEffects() {
        return this.removedEffects;
    }

    public Stream<Effect> getUnmutedEffects(boolean basic) {
        return this.getEffects(basic).stream()
                .filter(e -> !e.mute);
    }

    public Stream<Effect> getFinalEffects(boolean unmutedOnly) {
        Stream<Effect> ret = Stream.concat(this.getEffects(true).stream(), this.getEffects(false).stream());
        if (unmutedOnly) {
            return ret.filter(e -> !e.mute);
        } else {
            return ret;
        }
    }

    /**
     * Adds an effect to the card. May fail if the effect isn't stackable, and
     * there is already an instance of the effect.
     * 
     * @param basic Whether the effect is a basic effect of the card
     * @param pos   The position to add the effect to
     * @param e     The effect
     * @return Whether the effect as added successfully
     */
    public boolean addEffect(boolean basic, int pos, Effect e) {
        if (!e.stackable) {
            // check if another instance is already on this card
            for (Effect presentEffect : this.getEffects(basic)) {
                if (presentEffect.getClass().equals(e.getClass())) {
                    return false;
                }
            }
        }
        e.basic = basic;
        e.owner = this;
        e.removed = false;
        this.removedEffects.remove(e);
        this.getEffects(basic).add(pos, e);
        if (e.owner.board instanceof ServerBoard) {
            // because these types of effects are rare but need to be checked frequently,
            // register these to the ServerBoard to optimize lookup
            ServerBoard sb = (ServerBoard) e.owner.board;
            if (e instanceof EffectAura) {
                sb.auras.add((EffectAura) e);
            }
            if (e instanceof EffectWithDependentStats) {
                sb.dependentStats.add((EffectWithDependentStats) e);
            }
            if (e instanceof EffectUntilTurnEnd) {
                sb.effectsToRemoveAtEndOfTurn.add(e);
            }
            try {
                e.onListenEvent(null);
                // no exception, must have a listener implemented
                this.listeners.add(e);
                sb.listeners.add(this);
            } catch (UnsupportedOperationException ex) {
                // do nothing lol
            }
        }
        if (e.auraSource != null) {
            e.auraSource.currentActiveEffects.put(this, e);
        }
        this.updateEffectStats(basic);
        return true;
    }

    public boolean addEffect(boolean basic, Effect e) {
        return this.addEffect(basic, this.getEffects(basic).size(), e);
    }

    // purge: clean remove, don't even put it in the removedEffects
    public void removeEffect(Effect e, boolean purge) {
        if (this.effects.contains(e)) {
            this.effects.remove(e);
            if (!purge) {
                this.removedEffects.add(e);
            }
            if (e.owner.board instanceof ServerBoard) {
                ServerBoard sb = (ServerBoard) e.owner.board;
                if (e instanceof EffectAura) {
                    sb.auras.remove((EffectAura) e);
                }
                if (e instanceof EffectWithDependentStats) {
                    sb.dependentStats.remove((EffectWithDependentStats) e);
                }
                if (e instanceof EffectUntilTurnEnd) {
                    sb.effectsToRemoveAtEndOfTurn.remove(e);
                }
                try {
                    e.onListenEvent(null);
                    // no exception, must have a listener implemented
                    this.listeners.remove(e);
                    if (this.listeners.isEmpty()) {
                        sb.listeners.remove(this);
                    }
                } catch (UnsupportedOperationException ex) {
                    // do nothing lol
                }
            }
            if (e.auraSource != null) {
                e.auraSource.currentActiveEffects.remove(this);
                e.auraSource.lastCheckedAffectedCards.remove(this);
            }
            e.removed = true;
            this.updateEffectStats(false);
        }
    }

    public List<Effect> removeAdditionalEffects() {
        List<Effect> ret = new LinkedList<>();
        while (!this.effects.isEmpty()) {
            ret.add(this.effects.get(0));
            this.removeEffect(this.effects.get(0), false);
        }
        return ret;
    }

    public void muteEffect(Effect e, boolean mute) {
        if (this.effects.contains(e)) {
            e.mute = mute;
        }
        if (this.uiCard != null) {
            this.uiCard.updateIconList();
        }
    }

    // updates stat numbers, if a basic effect changed then it tallies the stat
    // numbers for both basic and additional effects, caching the tally for the
    // base stat numbers for future use
    public void updateEffectStats(boolean basic) {
        EffectStats.StatSet stats;
        if (basic) {
            stats = this.finalBasicStatEffects;
        } else {
            stats = this.finalStatEffects;
        }
        stats.reset();
        Stream<Effect> relevant = this.getEffects(true).stream().filter(e -> !e.bonusStats);
        if (!basic) {
            relevant = Stream.concat(
                    relevant,
                    Stream.concat(
                            this.getEffects(true).stream().filter(e -> e.bonusStats),
                            this.getEffects(false).stream()
                    )
            );
        }
        relevant.forEachOrdered(e -> e.effectStats.applyToStatSet(stats));
        stats.makeNonNegative();
        if (basic) {
            // update the stat numbers for the additional effects too
            this.updateEffectStats(false);
        }
        if (this.uiCard != null) {
            this.uiCard.updateIconList();
        }
    }

    // each returned resolver is wrapped to check for the predicate before execution
    protected ResolverQueue getResolvers(EventGroupType etype, List<Card> cards, Function<Effect, ResolverWithDescription> hook, Predicate<Effect> predicate) {
        return new ResolverQueue(this.getFinalEffects(true)
                .map(e -> {
                    ResolverWithDescription r = hook.apply(e);
                    if (r == null) {
                        return null;
                    }
                    return new HookResolver(etype, cards, r, e, predicate);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    // like above but for resolvers that require a list of targetlists, and we receive a list of list of targetlists
    // matches an entry in the input list of list of targetlists for each effect that requires a list of targetlists
    // kekl this method signature
    protected ResolverQueue getTargetedResolvers(EventGroupType etype,
                                                 List<Card> cards,
                                                 List<List<TargetList<?>>> targetsList,
                                                 BiFunction<Effect, List<TargetList<?>>, ResolverWithDescription> hook,
                                                 Predicate<Effect> predicate) {
        List<Effect> effects = this.getFinalEffects(true).collect(Collectors.toList());
        return new ResolverQueue(IntStream.range(0, effects.size())
                .mapToObj(i -> {
                    Effect e = effects.get(i);
                    ResolverWithDescription r = hook.apply(e, targetsList.get(i));
                    if (r == null) {
                        return null;
                    }
                    return new HookResolver(etype, cards, r, e, predicate);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public boolean canBePlayed() {
        return this.getBattlecryTargetingSchemes().stream().flatMap(Collection::stream).allMatch(TargetingScheme::conditions);
    }

    // probably not worth the hassle of making functional
    public List<List<TargetingScheme<?>>> getBattlecryTargetingSchemes() {
        List<List<TargetingScheme<?>>> list = new LinkedList<>();
        this.getFinalEffects(true).forEachOrdered(e -> list.add(e.getBattlecryTargetingSchemes()));
        return list;
    }

    @SuppressWarnings("unchecked")
    public boolean validateTargets(List<List<TargetingScheme<?>>> schemes, List<List<TargetList<?>>> targets) {
        for (int i = 0; i < schemes.size(); i++) {
            for (int j = 0; j < schemes.get(i).size(); j++) {
                if (!schemes.get(i).get(j).isValid((TargetList) targets.get(i).get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String targetsToString(List<List<TargetList<?>>> targets) {
        StringBuilder builder = new StringBuilder(targets.size() + " ");
        for (List<TargetList<?>> perEffectTargets : targets) {
            builder.append(Effect.targetsToString(perEffectTargets));
        }
        return builder.toString();
    }

    public static List<List<TargetList<?>>> targetsFromString(Board b, StringTokenizer st) {
        int num = Integer.parseInt(st.nextToken());
        List<List<TargetList<?>>> ret = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            ret.add(Effect.targetsFromString(b, st));
        }
        return ret;
    }

    public ResolverQueue battlecry(List<List<TargetList<?>>> targetsList) {
        return this.getTargetedResolvers(EventGroupType.BATTLECRY, List.of(this), targetsList, Effect::battlecry, eff -> !eff.removed);
    }

    public ResolverQueue onListenEvent(Event event) {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), eff -> eff.onListenEvent(event), eff -> true);
    }

    public String cardPosToString() {
        return this.status.toString() + " " + this.getIndex() + " ";
    }

    // TODO make a corresponding fromString method
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.appendStringToBuilder(builder);
        return builder.toString();
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        builder.append(this.getClass().getName()).append(" ").append(this.team).append(" ")
                .append(this.alive).append(" ").append(this.cardPosToString()).append(this.basicEffects.size()).append(" ");
        for (Effect e : this.basicEffects) {
            e.appendStringToBuilder(builder);
        }
        builder.append(this.effects.size()).append(" ");
        for (Effect e : this.effects) {
            e.appendStringToBuilder(builder);
        }
    }

    public String toReference() {
        return this.team + " " + this.cardPosToString();
    }

    public static String referenceOrNull(Card c) {
        return c == null ? "null " : c.toReference();
    }

    public static Card fromReference(Board b, StringTokenizer reference) {
        String firsttoken = reference.nextToken();
        if (firsttoken.equals("null")) {
            return null;
        }
        int team = Integer.parseInt(firsttoken);
        Player p = b.getPlayer(team);
        String sStatus = reference.nextToken();
        CardStatus csStatus = CardStatus.valueOf(sStatus);
        int cardpos = Integer.parseInt(reference.nextToken());
        if (cardpos == -1) { // mission failed we'll get em next time
            return null;
        }
        return switch (csStatus) {
            case HAND -> p.getHand().get(cardpos);
            case BOARD -> p.getPlayArea().get(cardpos);
            case DECK -> p.getDeck().get(cardpos);
            case GRAVEYARD -> p.getGraveyard().get(cardpos);
            case UNLEASHPOWER -> p.getUnleashPower().orElse(null);
            case LEADER -> p.getLeader().orElse(null);
            case BANISHED -> p.getBanished().get(cardpos);
        };
    }

    public static int compareDefault(Card a, Card b) {
        return (a.getTooltip().cost == b.getTooltip().cost) ? a.getClass().getName().compareTo(b.getClass().getName())
                : a.getTooltip().cost - b.getTooltip().cost;
    }

    @Override
    public int getIndex() {
        return this.cardpos;
    }

    @Override
    public void setIndex(int index) {
        this.cardpos = index;
    }

    public TooltipCard getTooltip() {
        return tooltip;
    }
}
