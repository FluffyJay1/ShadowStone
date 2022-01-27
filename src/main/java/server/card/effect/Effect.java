package server.card.effect;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

import client.*;
import server.*;
import server.card.*;
import server.card.target.CardTargetList;
import server.card.target.CardTargetingScheme;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.*;
import server.resolver.*;
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
    private int pos = 0;
    public Card owner = null;
    public String description;
    /*
     * Mute means the effect still provides stats, but won't do anything extra, it
     * can't listen for events or battlecry or unleash or anything
     */
    public boolean basic = false, mute = false, removed = false;

    public EffectStats effectStats = new EffectStats();
    /*
     * target specifications are set upon construction, player input fulfills the
     * targets
     */
    private List<TargetList<?>> battlecryTargets = new LinkedList<>(), unleashTargets = new LinkedList<>();

    public EffectAura auraSource;

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

    public Resolver battlecry() {
        return null;
    }

    public void setBattlecryTargets(List<TargetList<?>> targets) {
        this.battlecryTargets = targets;
    }

    public List<TargetList<?>> getBattlecryTargets() {
        return this.battlecryTargets;
    }

    public List<TargetingScheme<?>> getBattlecryTargetingSchemes() {
        return List.of();
    }

    // shameful glue
    public Stream<Card> getStillTargetableBattlecryCardTargets(int index) {
        return ((CardTargetList) this.getBattlecryTargets().get(index)).getStillTargetable((CardTargetingScheme) this.getBattlecryTargetingSchemes().get(index));
    }

    public Resolver unleash() {
        return null;
    }

    public void setUnleashTargets(List<TargetList<?>> targets) {
        this.unleashTargets = targets;
    }

    public List<TargetList<?>> getUnleashTargets() {
        return this.unleashTargets;
    }

    public List<TargetingScheme<?>> getUnleashTargetingSchemes() {
        return List.of();
    }

    // shameful glue
    public Stream<Card> getStillTargetableUnleashCardTargets(int index) {
        return ((CardTargetList) this.getUnleashTargets().get(index)).getStillTargetable((CardTargetingScheme) this.getUnleashTargetingSchemes().get(index));
    }

    public static List<TargetList<?>> parseTargets(StringTokenizer st, List<TargetingScheme<?>> schemes) {
        List<TargetList<?>> ret = new ArrayList<>(schemes.size());
        for (TargetingScheme<?> scheme : schemes) {
            ret.add(scheme.parseToList(st));
        }
        return ret;
    }

    // bruh
    @SuppressWarnings("unchecked")
    public static String targetsToString(List<TargetingScheme<?>> schemes, List<TargetList<?>> targetsList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < schemes.size(); i++) {
            TargetingScheme<?> scheme = schemes.get(i);
            TargetList targets = targetsList.get(i);
            builder.append(scheme.listToString(targets));
        }
        return builder.toString();
    }

    // these two are for the unleash power unleashing on a minion
    public Resolver onUnleashPre(Minion target) {
        return null;
    }

    public Resolver onUnleashPost(Minion target) {
        return null;
    }

    public Resolver onAttack(Minion target) {
        return null;
    }

    public Resolver onAttacked(Minion target) {
        return null;
    }

    public Resolver clash(Minion target) {
        return null;
    }

    public Resolver onDamaged(int damage) {
        return null;
    }

    public Resolver onTurnStart() {
        return null;
    }

    public Resolver onTurnEnd() {
        return null;
    }

    public Resolver onTurnStartEnemy() {
        return null;
    }

    public Resolver onTurnEndEnemy() {
        return null;
    }

    public Resolver lastWords() {
        return null;
    }

    public Resolver onEnterPlay() {
        return null;
    }

    public Resolver onLeavePlay() {
        return null;
    }

    // If this ever returns null, it is assumed to not have any listener at all,
    // if listening for a specific event, do the check in the resolver
    // specifically we check if the effect is an event listener if
    // onListenEvent(null) returns a non-null result (inside the Card class)
    public Resolver onListenEvent(Event event) {
        return null;
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
            Effect ef;
            ef = c.getDeclaredConstructor().newInstance();
            ef.description = description;
            ef.owner = owner;
            ef.auraSource = aura;
            ef.mute = mute;
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
        this.battlecryTargets = new LinkedList<>();
        for (TargetList<?> t : e.battlecryTargets) {
            this.battlecryTargets.add(t.clone());
        }
        this.unleashTargets = new LinkedList<>();
        for (TargetList<?> t : e.unleashTargets) {
            this.unleashTargets.add(t.clone());
        }
        return e;
    }

    public String toReference() {
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
