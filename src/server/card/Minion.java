package server.card;

import java.awt.Color;
import java.util.*;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.*;

import client.Game;
import client.tooltip.*;
import server.*;
import server.card.effect.*;
import server.event.*;
import server.event.Event;;

public class Minion extends BoardObject {
	public int health, attacksThisTurn = 0; // tempted to make damage an effect
	public boolean summoningSickness = true;

	public Minion(Board board, TooltipMinion tooltip) {
		super(board, tooltip);
		this.health = tooltip.health;
		Effect e = new Effect(0, "", tooltip.cost, tooltip.attack, tooltip.magic, tooltip.health, 1, false, false,
				false);
		this.addBasicEffect(e);
		if (tooltip.basicUnleash) {
			Effect unl = new Effect(0,
					"<b> Unleash: </b> Deal X damage to an enemy minion. X equals this minion's magic.") {
				@Override
				public EventFlag unleash() {
					EventFlag ef = new EventFlag(this) {
						@Override
						public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
							if (this.effect.unleashTargets.get(0) != null) {
								eventlist.add(new EventMinionDamage((Minion) this.effect.owner,
										this.effect.unleashTargets.get(0),
										this.effect.owner.finalStatEffects.getStat(EffectStats.MAGIC)));
							}
						}
					};
					return ef;
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
			this.addBasicEffect(unl);
		}

	}

	@Override
	public void drawOnBoard(Graphics g) {
		super.drawOnBoard(g);
		if (this.realCard != null && this.realCard instanceof Minion && ((Minion) this.realCard).canAttack()
				&& this.canAttack()) {
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
			i = i.getScaledCopy((float) this.scale);
			g.drawImage(i, this.pos.x - i.getWidth() / 2, this.pos.y - i.getHeight() / 2);
		}
		if (this.finalStatEffects.getStat(EffectStats.BANE) > 0) {
			Image i = Game.getImage("res/game/bane.png");
			i = i.getScaledCopy((float) this.scale);
			g.drawImage(i, this.pos.x - i.getWidth() / 2,
					this.pos.y - i.getHeight() / 2 + CARD_DIMENSIONS.y * (float) this.scale / 2);
		}
		if (this.finalStatEffects.getStat(EffectStats.POISONOUS) > 0) {
			Image i = Game.getImage("res/game/poisonous.png");
			i = i.getScaledCopy((float) this.scale);
			g.drawImage(i, this.pos.x - i.getWidth() / 2,
					this.pos.y - i.getHeight() / 2 + CARD_DIMENSIONS.y * (float) this.scale / 2);
		}
		this.drawOffensiveStat(g, this.finalStatEffects.getStat(EffectStats.ATTACK),
				this.finalBasicStatEffects.getStat(EffectStats.ATTACK), new Vector2f(-0.4f, 0.5f),
				new Vector2f(0, -0.5f), STAT_DEFAULT_SIZE);
		this.drawOffensiveStat(g, this.finalStatEffects.getStat(EffectStats.MAGIC),
				this.finalBasicStatEffects.getStat(EffectStats.MAGIC), new Vector2f(0, 0.5f), new Vector2f(0, -0.5f),
				STAT_DEFAULT_SIZE);
		this.drawHealthStat(g, this.health, this.finalStatEffects.getStat(EffectStats.HEALTH),
				this.finalBasicStatEffects.getStat(EffectStats.HEALTH), new Vector2f(0.4f, 0.5f),
				new Vector2f(0, -0.5f), STAT_DEFAULT_SIZE);
	}

	@Override
	public void drawInHand(Graphics g) {
		super.drawInHand(g);
		this.drawOffensiveStat(g, this.finalStatEffects.getStat(EffectStats.ATTACK),
				this.finalBasicStatEffects.getStat(EffectStats.ATTACK), new Vector2f(-0.4f, 0f), new Vector2f(0, -0.5f),
				STAT_DEFAULT_SIZE);
		this.drawOffensiveStat(g, this.finalStatEffects.getStat(EffectStats.MAGIC),
				this.finalBasicStatEffects.getStat(EffectStats.MAGIC), new Vector2f(-0.4f, 0.25f),
				new Vector2f(0, -0.5f), STAT_DEFAULT_SIZE);
		this.drawHealthStat(g, this.health, this.finalStatEffects.getStat(EffectStats.HEALTH),
				this.finalBasicStatEffects.getStat(EffectStats.HEALTH), new Vector2f(-0.4f, 0.5f),
				new Vector2f(0, -0.5f), STAT_DEFAULT_SIZE);
	}

	public void drawOffensiveStat(Graphics g, int stat, int basestat, Vector2f relpos, Vector2f textoffset,
			double fontsize) {
		Color c = Color.white;
		if (stat > basestat) {
			c = Color.green;
		}
		if (stat < basestat) {
			c = Color.orange;
		}
		this.drawStatNumber(g, stat, relpos, textoffset, fontsize, c);
	}

	public void drawHealthStat(Graphics g, int health, int maxhealth, int basehealth, Vector2f relpos,
			Vector2f textoffset, double fontsize) {
		Color c = Color.white;
		if (health < maxhealth) {
			c = Color.red;
		} else if (health > basehealth) {
			c = Color.green;
		}
		this.drawStatNumber(g, health, relpos, textoffset, fontsize, c);
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

	@Override
	public boolean isInPlay() {
		return this.alive && this.health > 0 && this.status.equals(CardStatus.BOARD);
	}

	public LinkedList<EventOnAttack> onAttack(Minion target) {
		LinkedList<EventOnAttack> list = new LinkedList<EventOnAttack>();
		for (Effect e : this.getFinalEffects()) {
			EventOnAttack temp = e.onAttack(target); // optimization probably
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public LinkedList<EventOnAttacked> onAttacked(Minion target) {
		LinkedList<EventOnAttacked> list = new LinkedList<EventOnAttacked>();
		for (Effect e : this.getFinalEffects()) {
			EventOnAttacked temp = e.onAttacked(target);
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public LinkedList<EventClash> clash(Minion target) {
		LinkedList<EventClash> list = new LinkedList<EventClash>();
		for (Effect e : this.getFinalEffects()) {
			EventClash temp = e.clash(target);
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public LinkedList<Event> onDamaged(int damage) {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onDamaged(damage);
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public LinkedList<EventFlag> unleash() {
		LinkedList<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.unleash();
			if (temp != null) {
				list.add(temp);
			}
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

	@Override
	public String toString() {
		return "Minion " + this.tooltip.name + " " + this.cardPosToString() + " " + alive + " "
				+ this.finalStatEffects.statsToString() + " " + this.health;
	}
}
