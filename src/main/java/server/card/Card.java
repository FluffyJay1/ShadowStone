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
import server.ai.AI;
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
    private static final int VALUE_MAX_REF_DEPTH = 3;
    public final Board board;
    public Player player; // functional dependency with team but who cares
    public boolean alive = true; // alive means not marked for death
    public int team;
    private int ref; // index in the board's cardTable
    private int cardpos;
    public int spellboosts;
    private final CardText cardText;
    private final TooltipCard tooltip;
    public CardStatus status;
    public Card realCard; // for visual board
    public Card visualCard; // for client board
    public UICard uiCard;
    public CardVisibility visibility;

    public EffectStats.StatSet finalStats = new EffectStats.StatSet(), finalBasicStats = new EffectStats.StatSet();
    public Set<CardTrait> finalTraits = new HashSet<>(), finalBasicTraits = new HashSet<>();
    /*
     * basic effects can't get removed unlike additional effects (e.g. bounce
     * effects), but they can be muted
     */
    public final PositionedList<Effect> effects = new PositionedList<>(new ArrayList<>());

    public final Set<Effect> listeners = new TreeSet<>(Comparator.comparing(Effect::getIndex));
    public final Set<Effect> whileInPlayListeners = new TreeSet<>(Comparator.comparing(Effect::getIndex));
    public final Set<Effect> whileInPlayStateTrackers = new TreeSet<>(Comparator.comparing(Effect::getIndex));

    public Card(Board board, CardText cardText) {
        this.board = board;
        this.cardText = cardText;
        this.tooltip = cardText.getTooltip();
        this.status = CardStatus.DECK;
        for (Effect e : cardText.getEffects()) {
            this.addEffect(true, e);
        }
        this.spellboosts = 0;
        this.visibility = CardVisibility.NONE;
    }

    // if card can be seen
    public boolean isVisibleTo(int team) {
        return switch (this.visibility) {
            case ALL -> true;
            case ALLIES -> this.team == team;
            case NONE -> false;
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

    public double getValue(int refs) {
        double sum = 0;
        if (this.finalStats.get(Stat.LIFESTEAL) > 0) {
            sum += AI.VALUE_OF_LIFESTEAL;
        }
        if (this.finalStats.get(Stat.POISONOUS) > 0) {
            sum += AI.VALUE_OF_POISONOUS;
        }
        if (this.finalStats.get(Stat.FREEZING_TOUCH) > 0) {
            sum += AI.VALUE_OF_FREEZING_TOUCH;
        }
        return sum;
    }

    public double getTotalEffectValueOf(Function<Effect, Double> property) {
        // functional is cool
        return this.getFinalEffects(true)
                .map(property)
                .reduce(0., Double::sum);
    }

    public Stream<Effect> getEffects(boolean basic) {
        return this.effects.stream().filter(e -> e.basic == basic && !e.removed);
    }

    public Stream<Effect> getFinalEffects(boolean unmutedOnly) {
        if (unmutedOnly) {
            return this.effects.stream().filter(e -> !e.mute && !e.removed);
        } else {
            return this.effects.stream().filter(e -> !e.removed);
        }
    }

    /**
     * Adds an effect to the card. May fail if the effect isn't stackable, and
     * there is already an instance of the effect.
     * 
     * @param basic Whether the effect is a basic effect of the card
     * @param e     The effect
     * @return Whether the effect as added successfully
     */
    public boolean addEffect(boolean basic, Effect e) {
        if (!e.stackable) {
            // check if another instance is already on this card
            if (this.getEffects(basic).anyMatch(presentEffect -> presentEffect.getClass().equals(e.getClass()))) {
                return false;
            }
        }
        e.basic = basic;
        e.owner = this;
        e.removed = false;
        this.effects.add(e);
        if (e.auraSource != null) {
            e.auraSource.currentActiveEffects.put(this, e);
        }
        this.updateEffectStats(basic);
        return true;
    }

    // purge: clean remove, don't even put it in the removedEffects
    public void removeEffect(Effect e, boolean purge) {
        if (purge) {
            this.effects.remove(e);
        }
        if (e.auraSource != null) {
            e.auraSource.currentActiveEffects.remove(this);
        }
        e.removed = true;
        this.updateEffectStats(e.basic);
    }

    // lol
    public void unremoveEffect(Effect e) {
        e.removed = false;
        this.updateEffectStats(e.basic);
    }

    public List<Effect> removeAdditionalEffects() {
        List<Effect> ret = this.getEffects(false).toList();
        this.getEffects(false).forEach(effect -> this.removeEffect(effect, false));
        return ret;
    }

    public void muteEffect(Effect e, boolean mute) {
        e.mute = mute;
        if (this.uiCard != null) {
            this.uiCard.updateIconList();
        }
    }

    // updates stat numbers, if a basic effect changed then it tallies the stat
    // numbers for both basic and additional effects, caching the tally for the
    // base stat numbers for future use
    public void updateEffectStats(boolean basic) {
        EffectStats.StatSet stats;
        Set<CardTrait> traits;
        Stream<Effect> relevant;
        if (basic) {
            stats = this.finalBasicStats;
            stats.reset();
            traits = this.finalBasicTraits;
            traits.clear();
            relevant = this.getEffects(true).filter(e -> !e.bonusStats);
        } else {
            stats = this.finalStats;
            stats.copy(this.finalBasicStats);
            traits = this.finalTraits;
            traits.clear();
            traits.addAll(this.finalBasicTraits);
            relevant = Stream.concat(
                    this.getEffects(true).filter(e -> e.bonusStats),
                    this.getEffects(false)
            );
        }
        relevant.forEachOrdered(e -> {
            e.effectStats.applyToStatSet(stats);
            traits.addAll(e.effectStats.traits);
        });
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
                    if (r == Effect.UNIMPLEMENTED_RESOLVER || r == null) {
                        return null;
                    }
                    return new HookResolver(etype, cards, r, e, predicate);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public boolean hasResolvers(Function<Effect, ResolverWithDescription> hook) {
        return this.getFinalEffects(true).anyMatch(e -> hook.apply(e) != Effect.UNIMPLEMENTED_RESOLVER);
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
                    if (r == Effect.UNIMPLEMENTED_RESOLVER || r == null) {
                        return null;
                    }
                    return new HookResolver(etype, cards, r, e, predicate);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public boolean canBePlayed() {
        return this.getBattlecryTargetingSchemes().stream().flatMap(Collection::stream).allMatch(TargetingScheme::conditions)
                && this.getFinalEffects(true).allMatch(Effect::battlecryPlayConditions);
    }

    public boolean canSpendAfterPlayed(int amount) {
        return this.player.mana >= this.finalStats.get(Stat.COST) + amount;
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
                TargetingScheme<?> scheme = schemes.get(i).get(j);
                if (scheme.isApplicable(targets.get(i)) && !scheme.isValid((TargetList) targets.get(i).get(j))) {
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
        return this.getTargetedResolvers(EventGroupType.BATTLECRY, List.of(this), targetsList, Effect::battlecry, eff -> !eff.removed && !eff.mute);
    }

    public ResolverQueue onListenEvent(Event event) {
        return new ResolverQueue(this.listeners.stream()
                .filter(e -> !e.mute)
                .map(e -> {
                    ResolverWithDescription r = e.onListenEvent(event);
                    if (r == null) {
                        return null;
                    }
                    return new HookResolver(EventGroupType.FLAG, List.of(this), r, e, eff -> !eff.removed && !eff.mute);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public ResolverQueue onListenEventWhileInPlay(Event event) {
        return new ResolverQueue(this.whileInPlayListeners.stream()
                .filter(e -> !e.mute)
                .map(e -> {
                    ResolverWithDescription r = e.onListenEventWhileInPlay(event);
                    if (r == null) {
                        return null;
                    }
                    return new HookResolver(EventGroupType.FLAG, List.of(this), r, e, eff -> !eff.removed && !eff.mute && eff.owner.isInPlay());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public void saveStateTrackers() {
        for (Effect e : this.whileInPlayStateTrackers) {
            e.lastCheckedStateToTrack = e.stateToTrack();
        }
    }

    public ResolverQueue onListenStateChangeWhileInPlay() {
        return new ResolverQueue(this.whileInPlayStateTrackers.stream()
                .filter(e -> !e.mute)
                .map(e -> {
                    Object newState = e.stateToTrack();
                    if (e.lastCheckedStateToTrack == null || Objects.equals(e.lastCheckedStateToTrack, newState)) {
                        return null;
                    }
                    ResolverWithDescription r = e.onListenStateChangeWhileInPlay(e.lastCheckedStateToTrack, newState);
                    if (r == null) {
                        return null;
                    }
                    return new HookResolver(EventGroupType.FLAG, List.of(this), r, e, eff -> !eff.removed && !eff.mute && eff.owner.isInPlay());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    public boolean battlecrySpecialConditions() {
        return this.getFinalEffects(true).anyMatch(Effect::battlecrySpecialConditions);
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
        builder.append(this.getRef()).append(" ").append(this.getClass().getName()).append(" ").append(this.team).append(" ")
                .append(this.alive).append(" ").append(this.cardPosToString()).append(this.spellboosts).append(" ").append(this.effects.size()).append(" ");
        for (Effect e : this.effects) {
            e.appendStringToBuilder(builder);
            builder.append(e.basic).append(" ").append(e.removed).append(" ");
        }
    }

    // for "summon a copy of x" effects
    // things like cardpos and basic effects don't matter
    public String toTemplateString() {
        StringBuilder builder = new StringBuilder();
        this.appendToTemplateStringBuilder(builder);
        return builder.toString();
    }

    public void appendToTemplateStringBuilder(StringBuilder builder) {
        builder.append(this.cardText.toString()).append(this.spellboosts).append(" ");
        List<Effect> additionalEffects = this.getEffects(false).toList();
        builder.append(additionalEffects.size()).append(" ");
        for (Effect e : additionalEffects) {
            e.appendStringToBuilder(builder);
        }
    }

    public static Card fromTemplateString(Board b, StringTokenizer st) {
        CardText cardText = CardText.fromString(st.nextToken());
        Card c = cardText.constructInstance(b);
        c.spellboosts = Integer.parseInt(st.nextToken());
        int numEffects = Integer.parseInt(st.nextToken());
        for (int i = 0; i < numEffects; i++) {
            Effect e = Effect.fromString(b, st);
            c.addEffect(false, e);
        }
        c.loadExtraTemplateStringParams(b, st);
        return c;
    }

    public void loadExtraTemplateStringParams(Board b, StringTokenizer st) {
        
    }

    public String toReference() {
        return this.getRef() + " ";
    }

    public static String referenceOrNull(Card c) {
        return c == null ? "null " : c.toReference();
    }

    public static Card fromReference(Board b, StringTokenizer reference) {
        String firsttoken = reference.nextToken();
        if (firsttoken.equals("null")) {
            return null;
        }
        int ref = Integer.parseInt(firsttoken);
        return b.cardTable.get(ref);
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

    public CardText getCardText() {
        return cardText;
    }

    public int getRef() {
        return ref;
    }

    public void setRef(int ref) {
        this.ref = ref;
    }
}
