package server.card.effect;

import java.lang.reflect.*;
import java.util.*;

import client.*;
import server.*;
import server.card.*;
import server.event.*;

/*
 * Few important things:
 * - Jank card effects are pretty much expected to be anonymous instances of this class
 * - Any subclasses must implement the (String, boolean) constructor for fromString reflection reasons also fuck you
 * - Anonymous classes cannot be recreated with fromString, so don't add that effect to other cards or don't make it anonymous
 * -- Anonymous effects solely tied to the construction of a card are fine, since they won't use fromString
 * - If a class saves state of some kind, it must implement extraStateString() and loadExtraState(Board, StringTokenizer)
 * -- If this state is mutable from in-game events, the effect must be marked as a listener (so AI can reset its state)
 */
public class Effect implements Cloneable {
	public int pos = 0;
	public Card owner = null;
	public String description;
	public boolean basic = false, mute = false, listener = false;

	public EffectStats set = new EffectStats(), change = new EffectStats();
	public List<Target> battlecryTargets = new LinkedList<Target>(), unleashTargets = new LinkedList<Target>();

	public Effect() {
		// TODO lmao
	}

	public Effect(String description, boolean listener) {
		this.description = description;
		this.listener = listener;
	}

	public Effect(String description, int cost) {
		this(description, false);
		this.set.setStat(EffectStats.COST, cost);
	}

	public Effect(String description, int cost, int attack, int magic, int health) {
		this(description, cost);
		this.set.setStat(EffectStats.ATTACK, attack);
		this.set.setStat(EffectStats.MAGIC, magic);
		this.set.setStat(EffectStats.HEALTH, health);
	}

	public Effect(String description, int cost, int attack, int magic, int health, int attacksperturn, boolean storm,
			boolean rush, boolean ward) {
		this(description, cost, attack, magic, health);
		this.set.setStat(EffectStats.ATTACKS_PER_TURN, attacksperturn);
		this.set.setStat(EffectStats.STORM, storm ? 1 : 0);
		this.set.setStat(EffectStats.RUSH, rush ? 1 : 0);
		this.set.setStat(EffectStats.WARD, ward ? 1 : 0);
	}

	public void applyEffectStats(Effect e) {
		for (int i = 0; i < e.set.stats.length; i++) {
			if (e.set.use[i]) {
				this.set.setStat(i, e.set.stats[i]);
				this.change.resetStat(i);
			}
		}
		for (int i = 0; i < e.change.stats.length; i++) {
			if (e.change.use[i]) {
				this.change.changeStat(i, e.change.stats[i]);
			}
		}
	}

	public Effect copyEffectStats() {
		Effect ret = new Effect(this.description, false);
		ret.applyEffectStats(this);
		return ret;
	}

	public void resetStats() {
		for (int i = 0; i < this.set.stats.length; i++) {
			this.set.resetStat(i);
		}
		for (int i = 0; i < this.change.stats.length; i++) {
			this.change.resetStat(i);
		}
	}

	public int getStat(int index) {
		return this.set.stats[index] + this.change.stats[index];
	}

	public boolean getUse(int index) {
		return this.set.use[index] || this.change.use[index];
	}

	public String statsToString() {
		return "(" + this.getStat(EffectStats.COST) + " " + this.getStat(EffectStats.ATTACK) + " "
				+ this.getStat(EffectStats.MAGIC) + " " + this.getStat(EffectStats.HEALTH) + ")";
	}

	public EventBattlecry battlecry() {
		return null;
	}

	public void setBattlecryTargets(List<Target> targets) {
		this.battlecryTargets = targets;
	}

	public void resetBattlecryTargets() {
		for (Target t : this.battlecryTargets) {
			t.reset();
		}
	}

	public Target getNextNeededBattlecryTarget() {
		for (Target t : this.battlecryTargets) {
			if (!t.isReady()) {
				return t;
			}
		}
		return null;
	}

	public EventFlag unleash() {
		return null;
	}

	public void setUnleashTargets(List<Target> targets) {
		this.unleashTargets = targets;
	}

	public void resetUnleashTargets() {
		for (Target t : this.unleashTargets) {
			t.reset();
		}
	}

	public Target getNextNeededUnleashTarget() {
		for (Target t : this.unleashTargets) {
			if (!t.isReady()) {
				return t;
			}
		}
		return null;
	}

	public EventOnAttack onAttack(Minion target) {
		return null;
	}

	public EventOnAttacked onAttacked(Minion target) {
		return null;
	}

	public EventClash clash(Minion target) {
		return null;
	}

	public EventFlag onDamaged(int damage) {
		return null;
	}

	public EventFlag onTurnStart() {
		return null;
	}

	public EventFlag onTurnEnd() {
		return null;
	}

	public EventLastWords lastWords() {
		return null;
	}

	public EventFlag onEnterPlay() {
		return null;
	}

	public EventFlag onLeavePlay() {
		return null;
	}

	// make sure to flag effect as an event listener
	public EventFlag onListenEvent(Event event) {
		return null;
	}

	@Override
	public String toString() {
		return this.getClass().getName() + " " + Card.referenceOrNull(this.owner) + this.description + Game.STRING_END
				+ " " + this.mute + " " + this.listener + " " + this.extraStateString() + this.set.toString()
				+ this.change.toString();
	}

	public static Effect fromString(Board b, StringTokenizer st) {
		try {
			String className = st.nextToken();
			Class<?> c = Class.forName(className);
			Card owner = Card.fromReference(b, st);
			String description = st.nextToken(Game.STRING_END).trim();
			st.nextToken(" \n"); // THANKS STRING TOKENIZER
			boolean mute = Boolean.parseBoolean(st.nextToken());
			boolean listener = Boolean.parseBoolean(st.nextToken());
			Effect ef;
			ef = (Effect) c.getDeclaredConstructor(String.class, boolean.class).newInstance(description, listener);
			ef.loadExtraState(b, st);
			ef.owner = owner;
			ef.mute = mute;
			ef.set = EffectStats.fromString(st);
			ef.change = EffectStats.fromString(st);
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
		return " ";
	}

	/*
	 * Some anonymous effects may have some extra state they keep track of that we
	 * can't really pass into as a constructor, to restore them we use this method
	 * that always returns this for convenience
	 */
	public Effect loadExtraState(Board b, StringTokenizer st) {
		return this;
	}

	@Override
	public Effect clone() throws CloneNotSupportedException {
		Effect e = (Effect) super.clone(); // shallow copy
		// e.mute = this.mute;
		// e.basic = this.basic;
		e.set = this.set.clone();
		e.change = this.change.clone();
		// TODO clone battlecry targets?
		this.battlecryTargets = new LinkedList<Target>();
		this.unleashTargets = new LinkedList<Target>();
		return e;
	}

	public String toReference() {
		return this.owner.toReference() + this.basic + " " + this.pos + " ";
	}

	public static String referenceOrNull(Effect effect) {
		return effect == null ? "null " : effect.toReference();
	}

	public static Effect fromReference(Board b, StringTokenizer st) {
		Card c = Card.fromReference(b, st);
		if (c == null) {
			return null;
		}
		boolean basic = Boolean.parseBoolean(st.nextToken());
		int pos = Integer.parseInt(st.nextToken());
		return c.getEffects(basic).get(pos);
	}

}
