package server.card.effect;

import java.util.LinkedList;

import server.card.*;
import server.event.Event;
import server.event.EventDraw;

public class Effect {
	public int id;
	public Card owner;
	public String description;
	public boolean mute = false;

	public EffectStats set = new EffectStats(), change = new EffectStats();
	public LinkedList<Target> battlecryTargets = new LinkedList<Target>(), unleashTargets = new LinkedList<Target>();

	public Effect(Card owner, int id, String description) {
		this.owner = owner;
		this.description = description;
	}

	public Effect(Card owner, int id, String description, int cost) {
		this(owner, id, description);
		this.set.setStat(EffectStats.COST_I, cost);
	}

	public Effect(Card owner, int id, String description, int cost, int attack, int magic, int health) {
		this(owner, id, description, cost);
		this.set.setStat(EffectStats.ATTACK_I, attack);
		this.set.setStat(EffectStats.MAGIC_I, magic);
		this.set.setStat(EffectStats.HEALTH_I, health);
	}

	public Effect applyEffectStats(Effect e) {
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
		return this;
	}

	public int getEffectStat(int index) {
		return this.set.stats[index] + this.change.stats[index];
	}

	public String statsToString() {
		return "(" + this.getEffectStat(EffectStats.COST_I) + " " + this.getEffectStat(EffectStats.ATTACK_I) + " "
				+ this.getEffectStat(EffectStats.MAGIC_I) + " " + this.getEffectStat(EffectStats.HEALTH_I) + ")";
	}

	public LinkedList<Event> battlecry() {
		return new LinkedList<Event>();
	}

	public void setBattlecryTargets(LinkedList<Target> targets) {
		this.battlecryTargets = targets;
	}

	public void resetBattlecryTargets() {
		for (Target t : this.battlecryTargets) {
			t.reset();
		}
	}

	public Target getNextNeededBattlecryTarget() {
		for (Target t : this.battlecryTargets) {
			if (!t.ready()) {
				return t;
			}
		}
		return null;
	}

	public String battlecryTargetsToString() {
		String ret = "btargets " + this.battlecryTargets.size();
		for (Target t : this.battlecryTargets) {
			ret += t.toString() + " ";
		}
		return ret;
	}

	public LinkedList<Event> unleash() {
		return new LinkedList<Event>();
	}

	public void setUnleashTargets(LinkedList<Target> targets) {
		this.unleashTargets = targets;
	}

	public void resetUnleashTargets() {
		for (Target t : this.unleashTargets) {
			t.reset();
		}
	}

	public Target getNextNeededUnleashTarget() {
		for (Target t : this.unleashTargets) {
			if (!t.ready()) {
				return t;
			}
		}
		return null;
	}

	public String unleashTargetsToString() {
		String ret = "utargets " + this.unleashTargets.size() + " ";
		for (Target t : this.unleashTargets) {
			ret += t.toString() + " ";
		}
		return ret;
	}

	public String toString() {
		return "" + this.id;
	}

	public LinkedList<Event> onAttack(Minion target) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> onAttacked(Minion target) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> clash(Minion target) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> onDamaged(int damage) {
		return new LinkedList<Event>();
	}
}
