package server.card;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
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
	public int health, attacksThisTurn = 0; // tempted to make damage an effect
	public boolean summoningSickness = true;

	public Minion(Board board, CardStatus status, int cost, int attack, int magic, int health, String name, String text,
			String imagepath, int team, int id) {
		super(board, status, cost, name, text, imagepath, team, id);
		this.health = health;
		this.addBasicEffect(new Effect(this, 0, "", cost, attack, magic, health, 1, false, false, false));
	}

	@Override
	public void drawOnBoard(Graphics g) {
		super.drawOnBoard(g);
		if (this.canAttack()) {
			g.setColor(org.newdawn.slick.Color.cyan);
			g.drawRect((float) (this.pos.x - CARD_DIMENSIONS.x * this.scale / 2),
					(float) (this.pos.y - CARD_DIMENSIONS.y * this.scale / 2), (float) (CARD_DIMENSIONS.x * this.scale),
					(float) (CARD_DIMENSIONS.y * this.scale));
			g.setColor(org.newdawn.slick.Color.white);
		}
		this.drawStatNumber(g, this.finalStatEffects.getStat(EffectStats.ATTACK),
				this.finalBasicStatEffects.getStat(EffectStats.ATTACK), false, new Vector2f(-0.4f, 0.5f),
				new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.finalStatEffects.getStat(EffectStats.MAGIC),
				this.finalBasicStatEffects.getStat(EffectStats.MAGIC), false, new Vector2f(0, 0.5f),
				new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.health, this.finalBasicStatEffects.getStat(EffectStats.HEALTH),
				this.health < this.finalStatEffects.getStat(EffectStats.HEALTH), new Vector2f(0.4f, 0.5f),
				new Vector2f(0, -0.5f));
	}

	@Override
	public void drawInHand(Graphics g) {
		super.drawInHand(g);
		this.drawStatNumber(g, this.finalStatEffects.getStat(EffectStats.ATTACK),
				this.finalBasicStatEffects.getStat(EffectStats.ATTACK), false, new Vector2f(-0.4f, 0f),
				new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.finalStatEffects.getStat(EffectStats.MAGIC),
				this.finalBasicStatEffects.getStat(EffectStats.MAGIC), false, new Vector2f(-0.4f, 0.25f),
				new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.health, this.finalBasicStatEffects.getStat(EffectStats.HEALTH),
				this.health < this.finalStatEffects.getStat(EffectStats.HEALTH), new Vector2f(-0.4f, 0.5f),
				new Vector2f(0, -0.5f));
	}

	public ArrayList<Minion> getAttackableTargets() {
		if (this.summoningSickness && (this.finalStatEffects.getStat(EffectStats.STORM) == 0
				&& this.finalStatEffects.getStat(EffectStats.RUSH) == 0)) {
			return new ArrayList<Minion>();
		}
		ArrayList<Minion> list = new ArrayList<Minion>();
		ArrayList<BoardObject> poss = this.board.getBoardObjects(this.team * -1);
		ArrayList<Minion> wards = new ArrayList<Minion>();
		// check for ward
		boolean ward = false;
		for (BoardObject b : poss) {
			if (b instanceof Leader
					&& (!this.summoningSickness || this.finalStatEffects.getStat(EffectStats.STORM) > 0)) {
				list.add((Leader) b);
			} else if (b instanceof Minion
					&& (!this.summoningSickness || this.finalStatEffects.getStat(EffectStats.RUSH) > 0
							|| this.finalStatEffects.getStat(EffectStats.STORM) > 0)) {
				// TODO add if can attack this minion
				list.add((Minion) b);
				if (((Minion) b).finalStatEffects.getStat(EffectStats.WARD) > 0) {
					ward = true;
					wards.add((Minion) b);
				}
			}
			// TODO add restrictions on can't attack leader
		}
		if (ward) {
			return wards;
		}
		return list;
	}

	public boolean canAttack() {
		return this.team == this.board.currentplayerturn && this.getAttackableTargets().size() > 0
				&& this.attacksThisTurn < this.finalStatEffects.getStat(EffectStats.ATTACKS_PER_TURN);
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
