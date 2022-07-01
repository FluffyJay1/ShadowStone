package server.card.effect;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import client.*;
import server.*;
import server.card.*;
import server.card.target.CardTargetList;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.*;
import server.resolver.meta.ResolverWithDescription;
import utils.Indexable;
import utils.StringBuildable;

/*
 * Few important things:
 * - Jank card effects are pretty much expected to be anonymous instances of this class
 * - Any subclasses must implement the default constructor for fromString reflection reasons also fuck you
 * - Anonymous classes cannot be recreated with fromString, so don't add that effect to other cards or don't make it anonymous
 * -- Anonymous effects solely tied to the construction of a card are fine, since they won't use fromString
 * - If a class saves state of some kind, it must implement extraStateString() and loadExtraState(Board, StringTokenizer)
 * -- This is used to allow AI to undo moves
 * - Effects should override getBattlecryValue() and getPresenceValue() for the AI
 */
public class Effect implements Indexable, StringBuildable, Cloneable {
    public static final ResolverWithDescription UNIMPLEMENTED_RESOLVER = new ResolverWithDescription("", null);
    private int pos = 0;
    public Card owner = null;
    public String description;
    public boolean stackable = true;
    /*
     * Mute means the effect still provides stats, but won't do anything extra, it
     * can't listen for events or battlecry or unleash or anything
     */
    public boolean basic = false, mute = false, removed = false;

    // if a basic effect, notes if the stats should be considered as additional stats
    // for e.g. cost reduction by spellboosting
    public boolean bonusStats = false;

    public EffectStats effectStats = new EffectStats();

    public EffectAura auraSource;

    // for effects that last until the end of turn
    // When the Card adds one of these effects, it keeps track in the
    // ServerBoard, then the TurnEndResolver checks these and removes them
    // 1 is on friendly team end, 0 for either team, -1 for enemy team end, null for nothing at all
    public Integer untilTurnEndTeam = null;

    // who needs a factory

    public Effect() {
        this.description = "";
    }

    public Effect(String description) {
        this.description = description;
    }

    public Effect(String description, EffectStats stats) {
        this(description);
        this.effectStats = stats;
    }

    public Effect(String description, EffectStats stats, Consumer<Effect> setters) {
        this(description, stats);
        setters.accept(this);
    }

    public ResolverWithDescription battlecry(List<TargetList<?>> targetList) {
        return null;
    }

    public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
        return List.of();
    }

    // for preventing playing some cards outside of the targeting requirement, e.g. resurrecting a minion when no minions have died
    public boolean battlecryPlayConditions() {
        return true;
    }

    // to display a yellow highlight around the card in hand if a special effect can be triggered
    public boolean battlecrySpecialConditions() {
        return false;
    }

    public ResolverWithDescription unleash(List<TargetList<?>> targetList) {
        return null;
    }

    public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
        return List.of();
    }

    // to display a yellow highlight around the card while drag unleashing if a special effect can be triggered
    public boolean unleashSpecialConditions() {
        return false;
    }

    // shameful glue
    public Stream<Card> getStillTargetableCards(Function<Effect, List<TargetingScheme<?>>> schemesFrom, List<TargetList<?>> targetList, int index) {
        return ((CardTargetList) targetList.get(index)).getStillTargetable((CardTargetingScheme) schemesFrom.apply(this).get(index));
    }

    public static List<TargetList<?>> targetsFromString(Board b, StringTokenizer st) {
        int num = Integer.parseInt(st.nextToken());
        List<TargetList<?>> ret = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            ret.add(TargetList.createFromString(b, st));
        }
        return ret;
    }

    // bruh
    public static String targetsToString(List<TargetList<?>> targetsList) {
        StringBuilder builder = new StringBuilder(targetsList.size() + " ");
        for (TargetList<?> targets : targetsList) {
            builder.append(targets.toString());
        }
        return builder.toString();
    }

    // these two are for the unleash power unleashing on a minion
    // can assume that the unleash power and the minion is in play
    public ResolverWithDescription onUnleashPre(Minion target) {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that the unleash power and the minion is in play
    public ResolverWithDescription onUnleashPost(Minion target) {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that both minions are in play
    public ResolverWithDescription strike(Minion target) {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that both minions are in play
    public ResolverWithDescription minionStrike(Minion target) {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that both minions are in play
    public ResolverWithDescription leaderStrike(Leader target) {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that both minions are in play
    public ResolverWithDescription retaliate(Minion target) {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that both minions are in play
    public ResolverWithDescription clash(Minion target) {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that minion is in play
    public ResolverWithDescription onDamaged(int damage) {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that boardobject is in play
    public ResolverWithDescription onTurnStartAllied() {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that boardobject is in play
    public ResolverWithDescription onTurnEndAllied() {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that boardobject is in play
    public ResolverWithDescription onTurnStartEnemy() {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that boardobject is in play
    public ResolverWithDescription onTurnEndEnemy() {
        return UNIMPLEMENTED_RESOLVER;
    }

    // anything goes
    public ResolverWithDescription lastWords() {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that boardobject is in play
    public ResolverWithDescription onEnterPlay() {
        return UNIMPLEMENTED_RESOLVER;
    }

    // can assume that boardobject is not in play
    public ResolverWithDescription onLeavePlay() {
        return UNIMPLEMENTED_RESOLVER;
    }

    /*
    For optimization, check the event instance before deciding to return a
    resolver or null, then check the details of the event inside the resolver
    if this returns UNIMPLEMENTED_RESOLVER, then we know it doesn't have a listener
     */
    public ResolverWithDescription onListenEvent(Event event) {
        return UNIMPLEMENTED_RESOLVER;
    }

    // for more optimization
    public ResolverWithDescription onListenEventWhileInPlay(Event event) {
        return UNIMPLEMENTED_RESOLVER;
    }

    /**
     * Effects can have wacky special effects and hooks, but the value of having
     * these effects isn't reflected by other means, so we have to manually
     * ascribe a value to it
     *
     * This could be solved if the AI could learn the proper value of the
     * specific effect, either through extensive high-depth simulation or via
     * some weights learning process, but fuck it
     *
     * Anyway, we pin a value on the special part of the effect, i.e. determine
     * the value we lose if we were to suddenly mute this effect. These should
     * not be complex calculations, but instead be mere estimates on the future
     * value that an effect may bring. A good rule of thumb for effects with
     * varying outcomes is (max value)/2.
     *
     * @param refs The depth of how many other cards we're allowed to refer to for value calculation
     * @return The approximate value of the special parts of the effect
     */
    public double getPresenceValue(int refs) {
        return 0;
    }

    /**
     * same deal but for battlecries, we don't consider this value anymore
     * after we play the card
     *
     * This value represents some kind of expected or potential future value of
     * playing the card, used for the AI to determine basically whether it's
     * worth playing the card or not
     *
     * @param refs The depth of how many other cards we're allowed to refer to for value calculation
     * @return The approximate value of playing the card
     */
    public double getBattlecryValue(int refs) {
        return 0;
    }

    /**
     * Same deal but for last words
     *
     * Some cards we want to die to trigger the juicy last words effect, so we
     * make this score increase as the card gets closer to dying.
     *
     * @param refs The depth of how many other cards we're allowed to refer to for value calculation
     * @return The approximate mana value of this card being destroyed
     */
    public double getLastWordsValue(int refs) {
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.appendStringToBuilder(builder);
        return builder.toString();
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        builder.append(this.getClass().getName()).append(" ").append(Card.referenceOrNull(this.owner))
                .append(Effect.referenceOrNull(this.auraSource)).append(this.description)
                .append(Game.STRING_END).append(" ").append(this.mute).append(" ")
                .append(this.untilTurnEndTeam == null ? "null" : this.untilTurnEndTeam).append(" ")
                .append(this.extraStateString());
        this.effectStats.appendStringToBuilder(builder);
    }

    public static Effect fromString(Board b, StringTokenizer st) {
        try {
            String className = st.nextToken();
            Class<? extends Effect> c = Class.forName(className).asSubclass(Effect.class);
            Card owner = Card.fromReference(b, st);
            EffectAura aura = (EffectAura) Effect.fromReference(b, st);
            String description = st.nextToken(Game.STRING_END).trim();
            st.nextToken(" \n"); // THANKS STRING TOKENIZER
            boolean mute = Boolean.parseBoolean(st.nextToken());
            String untilTurnEndTeamString = st.nextToken();
            Integer untilTurnEndTeam = untilTurnEndTeamString.equals("null") ? null : Integer.valueOf(untilTurnEndTeamString);
            Effect ef;
            ef = c.getDeclaredConstructor().newInstance();
            ef.description = description;
            ef.owner = owner;
            ef.auraSource = aura;
            ef.mute = mute;
            ef.untilTurnEndTeam = untilTurnEndTeam;
            ef.loadExtraState(b, st);
            ef.effectStats = EffectStats.fromString(st);
            return ef;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    // override this shit, end with space
    public String extraStateString() {
        return "";
    }

    /*
     * Some anonymous effects may have some extra state they keep track of that we
     * can't really pass into as a constructor, to restore them we use this method
     */
    public void loadExtraState(Board b, StringTokenizer st) {

    }

    @Override
    public Effect clone() throws CloneNotSupportedException {
        Effect e = (Effect) super.clone(); // shallow copy
        e.effectStats = this.effectStats.clone();
        return e;
    }

    public String toReference() {
        if (this.owner == null) {
            return "null ";
        }
        return this.owner.toReference() + this.basic + " " + this.removed + " " + this.getIndex() + " ";
    }

    public static String referenceOrNull(Effect effect) {
        return effect == null ? "null " : effect.toReference();
    }

    // handles the null case too
    public static Effect fromReference(Board b, StringTokenizer st) {
        Card c = Card.fromReference(b, st);
        if (c == null) {
            return null;
        }
        boolean basic = Boolean.parseBoolean(st.nextToken());
        boolean removed = Boolean.parseBoolean(st.nextToken());
        int pos = Integer.parseInt(st.nextToken());
        if (removed) {
            return c.getRemovedEffects().get(pos);
        }
        return c.getEffects(basic).get(pos);
    }

    @Override
    public int getIndex() {
        return this.pos;
    }

    @Override
    public void setIndex(int index) {
        this.pos = index;
    }
}
