package server.card;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import server.Board;
import server.card.effect.Effect;
import server.card.effect.EffectStatChange;
import server.card.effect.EffectStats;
import server.event.Event;
import server.event.EventAddEffect;
import server.event.EventDamage;
import server.event.EventDraw;
import server.event.EventMinionDamage;

public class Minion extends BoardObject {
	public int health, attacksThisTurn = 0; // tempted to make damage an effect
	public boolean summoningSickness = true;

	public Minion(Board board, CardStatus status, int cost, int attack, int magic, int health, boolean basicUnleash,
			String name, String text, String imagepath, int team, int id) {
		super(board, status, cost, name, text, imagepath, team, id);
		this.health = health;
		Effect e = new Effect(0, "", cost, attack, magic, health, 1, false, false, false);
		this.addBasicEffect(e);
		if (basicUnleash) {
			Effect unl = new Effect(0,
					"<b> Unleash: </b> Deal X damage to an enemy minion. X equals this minion's magic.") {
				@Override
				public LinkedList<Event> unleash() {
					LinkedList<Event> list = new LinkedList<Event>();
					if (this.unleashTargets.get(0) != null) {
						list.add(new EventMinionDamage((Minion) this.owner, this.unleashTargets.get(0).copy(),
								this.owner.finalStatEffects.getStat(EffectStats.MAGIC)));
					}
					// targets are reset in eventunleash
					return list;
				}
			};
			Target t = new Target(unl, 1, "Deal X damage to an enemy minion. X equals this minion's magic.") {
				@Override
				public boolean canTarget(Card c) {
					return c.status == CardStatus.BOARD && c instanceof Minion && !(c instanceof Leader)
							&& ((Minion) c).team != this.getCreator().owner.team;
				}
			};
			LinkedList<Target> list = new LinkedList<Target>();
			list.add(t);
			unl.setUnleashTargets(list);
			if (!text.isEmpty()) {
				this.text += "\n";
			}
			this.text += "<b> Unleash: </b> Deal X damage to an enemy minion. X equals this minion's magic.";
			this.addBasicEffect(unl);
		}

	}

	@Override
	public void drawOnBoard(Graphics g) {
		super.drawOnBoard(g);
		if (this.realCard != null && this.realCard instanceof Minion && ((Minion) this.realCard).canAttack()
				&& this.team == this.board.currentplayerturn) {
			if (this.summoningSickness && ((Minion) this.realCard).finalStatEffects.getStat(EffectStats.RUSH) > 0
					&& ((Minion) this.realCard).finalStatEffects.getStat(EffectStats.STORM) == 0) {
				g.setColor(org.newdawn.slick.Color.yellow);
			} else {
				g.setColor(org.newdawn.slick.Color.cyan);
			}

			g.drawRect((float) (this.pos.x - CARD_DIMENSIONS.x * this.scale / 2),
					(float) (this.pos.y - CARD_DIMENSIONS.y * this.scale / 2), (float) (CARD_DIMENSIONS.x * this.scale),
					(float) (CARD_DIMENSIONS.y * this.scale));
			g.setColor(org.newdawn.slick.Color.white);
		}
		if (this.finalStatEffects.getStat(EffectStats.WARD) > 0) {
			Image i = Game.getImage("res/game/shield.png");
			g.drawImage(i, this.pos.x - i.getWidth() / 2, this.pos.y - i.getHeight() / 2);
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
			if (b instanceof Leader) {
				if (!this.summoningSickness || this.finalStatEffects.getStat(EffectStats.STORM) > 0) {
					list.add((Leader) b);
				}
			} else if (b instanceof Minion) {
				if (!this.summoningSickness || this.finalStatEffects.getStat(EffectStats.RUSH) > 0
						|| this.finalStatEffects.getStat(EffectStats.STORM) > 0) {
					// TODO add if can attack this minion eg stealth or can't be
					// attacked
					list.add((Minion) b);
				}
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
		String ret = list.size() + " ";
		for (Target t : list) {
			ret += t.toString();
		}
		return ret;
	}

	public void unleashTargetsFromString(Board b, StringTokenizer st) {
		int num = Integer.parseInt(st.nextToken());
		for (int i = 0; i < num; i++) {
			this.getUnleashTargets().get(i).copyFromString(b, st);
		}
	}

	public String toString() {
		return "Minion " + name + " " + this.cardPosToString() + " " + alive + " "
				+ this.finalStatEffects.statsToString() + " " + this.health;
	}
}
