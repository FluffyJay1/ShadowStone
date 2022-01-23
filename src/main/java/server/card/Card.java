package server.card;

import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import client.tooltip.*;
import client.ui.game.*;
import server.*;
import server.card.effect.*;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.resolver.*;
import utils.Indexable;
import utils.PositionedList;
import utils.StringBuildable;

public abstract class Card implements Indexable, StringBuildable {
    // getValue may depend on the value of other cards, put a limit to how many
    // other cards it needs to calculate
    private static final int VALUE_MAX_REF_DEPTH = 5;
    public final Board board;
    public boolean alive = true; // alive means not marked for death
    public int team;
    private int cardpos;
    private final TooltipCard tooltip;
    public CardStatus status;
    public final ClassCraft craft;
    public Card realCard; // for visual board
    public Card visualCard; // for client board
    public UICard uiCard;

    public EffectStats.StatSet finalStatEffects = new EffectStats.StatSet(), finalBasicStatEffects = new EffectStats.StatSet();
    /*
     * basic effects can't get removed unlike additional effects (e.g. bounce
     * effects), but they can be muted
     */
    private final PositionedList<Effect> effects = new PositionedList<>(new ArrayList<>()),
            basicEffects = new PositionedList<>(new ArrayList<>()),
            removedEffects = new PositionedList<>(new ArrayList<>());
    // for convenience, a subset of above effects that are listeners
    public final List<Effect> listeners = new LinkedList<>();
    // same but for auras, however, removing the effect doesn't remove it from this list
    public final List<EffectAura> auras = new LinkedList<>();

    public Card(Board board, TooltipCard tooltip) {
        this.board = board;
        this.tooltip = tooltip;
        this.status = CardStatus.DECK;
        this.craft = tooltip.craft;
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
        return this.getFinalEffects(true).stream()
                .map(property)
                .reduce(0., Double::sum);
    }

    public List<Effect> getEffects(boolean basic) {
        return basic ? this.basicEffects : this.effects;
    }

    public List<Effect> getRemovedEffects() {
        return this.removedEffects;
    }

    public List<Effect> getUnmutedEffects(boolean basic) {
        LinkedList<Effect> list = new LinkedList<>();
        for (Effect e : this.getEffects(basic)) {
            if (!e.mute) {
                list.add(e);
            }
        }
        return list;
    }

    public List<Effect> getFinalEffects(boolean unmutedOnly) {
        LinkedList<Effect> list = new LinkedList<>();
        if (unmutedOnly) {
            list.addAll(this.getUnmutedEffects(true));
            list.addAll(this.getUnmutedEffects(false));
        } else {
            list.addAll(this.getEffects(true));
            list.addAll(this.getEffects(false));
        }
        return list;
    }

    /**
     * Adds an effect to the card. If the effect is also flagged as an event
     * listener, it is registered with the board.
     * 
     * @param basic Whether the effect is a basic effect of the card
     * @param pos   The position to add the effect to
     * @param e     The effect
     */
    public void addEffect(boolean basic, int pos, Effect e) {
        e.basic = basic;
        e.owner = this;
        e.removed = false;
        this.removedEffects.remove(e);
        this.getEffects(basic).add(pos, e);
        if (e.onListenEvent(null) != null) {
            this.listeners.add(e);
        }
        if (e instanceof EffectAura) {
            this.auras.add((EffectAura) e);
        }
        if (e.auraSource != null) {
            e.auraSource.currentActiveEffects.put(this, e);
        }
        this.updateEffectStats(basic);
    }

    public void addEffect(boolean basic, Effect e) {
        this.addEffect(basic, this.getEffects(basic).size(), e);
    }

    // purge: clean remove, don't even put it in the removedEffects
    public void removeEffect(Effect e, boolean purge) {
        if (this.effects.contains(e)) {
            this.effects.remove(e);
            if (!purge) {
                this.removedEffects.add(e);
            } else if (e instanceof EffectAura) {
                this.auras.remove((EffectAura) e);
            }
            if (e.auraSource != null) {
                e.auraSource.currentActiveEffects.remove(this);
            }
            e.removed = true;
            if (e.onListenEvent(null) != null) {
                this.listeners.remove(e);
            }
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
        List<Effect> relevant = basic ? this.getEffects(true) : this.getFinalEffects(false);
        for (Effect e : relevant) {
            e.effectStats.applyToStatSet(stats);
        }
        stats.makeNonNegative();
        if (basic) {
            // update the stat numbers for the additional effects too
            this.updateEffectStats(false);
        }
        if (this.uiCard != null) {
            this.uiCard.updateIconList();
        }
    }

    public List<Resolver> getResolvers(Function<Effect, Resolver> hook) {
        return this.getFinalEffects(true).stream()
                .map(hook)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean canBePlayed() {
        return this.getBattlecryTargetingSchemes().stream().allMatch(TargetingScheme::conditions);
    }

    // probably not worth the hassle of making functional
    public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
        List<TargetingScheme<?>> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            list.addAll(e.getBattlecryTargetingSchemes());
        }
        return list;
    }

    public List<TargetList<?>> getTargets(Function<Effect, List<TargetList<?>>> targetGetter) {
        List<TargetList<?>> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            list.addAll(targetGetter.apply(e));
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public boolean validateTargets(List<TargetingScheme<?>> schemes, List<TargetList<?>> targets) {
        for (int i = 0; i < schemes.size(); i++) {
            if (!schemes.get(i).isValid((TargetList) targets.get(i))) {
                return false;
            }
        }
        return true;
    }

    public void setBattlecryTargets(List<TargetList<?>> targets) {
        int start = 0;
        for (Effect e : this.getFinalEffects(true)) {
            int end = start + e.getBattlecryTargetingSchemes().size();
            e.setBattlecryTargets(targets.subList(start, end));
            start = end;
        }
        assert start == targets.size();
    }

    public String battlecryTargetsToString(List<TargetList<?>> targets) {
        int start = 0;
        StringBuilder builder = new StringBuilder();
        for (Effect e : this.getFinalEffects(true)) {
            List<TargetingScheme<?>> battlecryTargetingSchemes = e.getBattlecryTargetingSchemes();
            int end = start + battlecryTargetingSchemes.size();
            builder.append(Effect.targetsToString(battlecryTargetingSchemes, targets.subList(start, end)));
            start = end;
        }
        return builder.toString();
    }

    public List<TargetList<?>> parseBattlecryTargets(StringTokenizer st) {
        List<TargetList<?>> ret = new ArrayList<>();
        for (Effect e : this.getFinalEffects(true)) {
            Effect.parseTargets(st, e.getBattlecryTargetingSchemes(), ret);
        }
        return ret;
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

    public String toConstructorString() {
        return this.getClass().getName() + " ";
    }

    public static Card createFromConstructorString(Board b, StringTokenizer st) {
        Class<? extends Card> cardClass;
        try {
            cardClass = Class.forName(st.nextToken()).asSubclass(Card.class);
            return createFromConstructor(b, cardClass);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    public static Card createFromConstructor(Board b, Class<? extends Card> cardClass) {
        Card card = null;
        try {
            card = cardClass.getConstructor(Board.class).newInstance(b);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return card;
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
