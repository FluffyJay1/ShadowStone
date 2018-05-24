package server.card;

import java.awt.Color;
import java.awt.Font;
import java.util.LinkedList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import server.Board;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.Event;
import server.event.EventDamage;
import server.event.EventDraw;

public class Minion extends BoardObject {
	public static final double STAT_DEFAULT_SIZE = 24;
	public int health; // tempted to make damage an effect

	public Minion(Board board, CardStatus status, int cost, int attack, int magic, int health, String name, String text,
			String imagepath, int team, int id) {
		super(board, status, cost, name, text, imagepath, team, id);
		this.health = health;
		this.addBasicEffect(new Effect(this, 0, "", cost, attack, magic, health));
	}

	@Override
	public void drawOnBoard(Graphics g) {
		this.drawStatNumber(g, this.finalStatEffects.getEffectStat(EffectStats.ATTACK_I),
				this.finalBasicStatEffects.getEffectStat(EffectStats.ATTACK_I), false, new Vector2f(-0.4f, 0.5f),
				new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.finalStatEffects.getEffectStat(EffectStats.MAGIC_I),
				this.finalBasicStatEffects.getEffectStat(EffectStats.MAGIC_I), false, new Vector2f(0, 0.5f),
				new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.health, this.finalBasicStatEffects.getEffectStat(EffectStats.HEALTH_I),
				this.health < this.finalStatEffects.getEffectStat(EffectStats.HEALTH_I), new Vector2f(0.4f, 0.5f),
				new Vector2f(0, -0.5f));
	}

	@Override
	public void drawInHand(Graphics g) {
		this.drawStatNumber(g, this.finalStatEffects.getEffectStat(EffectStats.ATTACK_I),
				this.finalBasicStatEffects.getEffectStat(EffectStats.ATTACK_I), false, new Vector2f(-0.4f, 0f),
				new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.finalStatEffects.getEffectStat(EffectStats.MAGIC_I),
				this.finalBasicStatEffects.getEffectStat(EffectStats.MAGIC_I), false, new Vector2f(-0.4f, 0.25f),
				new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.health, this.finalBasicStatEffects.getEffectStat(EffectStats.HEALTH_I),
				this.health < this.finalStatEffects.getEffectStat(EffectStats.HEALTH_I), new Vector2f(-0.4f, 0.5f),
				new Vector2f(0, -0.5f));
	}

	private void drawStatNumber(Graphics g, int stat, int basestat, boolean damaged, Vector2f relpos,
			Vector2f textoffset) {
		Color c = Color.white;
		if (damaged) {
			c = Color.red;
		} else {
			if (stat > basestat) {
				c = Color.green;
			}
			if (stat < basestat) {
				c = Color.orange;
			}
		}
		UnicodeFont font = Game.getFont("Verdana", STAT_DEFAULT_SIZE * this.scale, true, false, c, Color.BLACK);
		font.drawString(
				this.pos.x + CARD_DIMENSIONS.x * relpos.x * (float) this.scale
						+ font.getWidth("" + stat) * (textoffset.x - 0.5f),
				this.pos.y + CARD_DIMENSIONS.y * relpos.y * (float) this.scale
						+ font.getHeight("" + stat) * (textoffset.y - 0.5f),
				"" + stat);
	}

	public LinkedList<Event> onAttack(Minion target) {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.onAttack(target));
		}
		return list;
	}

	public LinkedList<Event> onAttacked(Minion target) {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.onAttacked(target));
		}
		return list;
	}

	public LinkedList<Event> clash(Minion target) {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.clash(target));
		}
		return list;
	}

	public LinkedList<Event> onDamaged(int damage) {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.onDamaged(damage));
		}
		return list;
	}

	public LinkedList<Event> unleash() {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.unleash());
		}
		return list;
		// return new LinkedList<Event>();
	}

	public LinkedList<Target> getUnleashTargets() {
		LinkedList<Target> list = new LinkedList<Target>();
		for (Effect e : this.getFinalEffects()) {
			for (Target t : e.unleashTargets) {
				list.add(t);
			}
		}
		return list;
	}

	public void resetUnleashTargets() {
		for (Target t : this.getUnleashTargets()) {
			t.reset();
		}
	}

	public Target getNextNeededUnleashTarget() {
		for (Target t : this.getUnleashTargets()) {
			if (!t.ready()) {
				return t;
			}
		}
		return null;
	}

	public String unleashTargetsToString() {
		LinkedList<Target> list = this.getUnleashTargets();
		String ret = "utargets " + list.size() + " ";
		for (Target t : list) {
			ret += t.toString() + " ";
		}
		return ret;
	}

	public String toString() {
		return "Minion " + name + " " + this.posToString() + " alive " + alive + "\n"
				+ this.finalStatEffects.statsToString();
	}
}
