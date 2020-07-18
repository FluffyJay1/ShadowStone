package server.card;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.effect.*;
import server.event.*;;

public class Minion extends BoardObject {
	public int health, attacksThisTurn = 0; // tempted to make damage an effect
	public boolean summoningSickness = true;

	public Minion(Board board, TooltipMinion tooltip) {
		super(board, tooltip);
		this.health = tooltip.health;
		Effect e = new Effect(0, "", tooltip.cost, tooltip.attack, tooltip.magic, tooltip.health, 1, false, false,
				false);
		this.addEffect(true, e);
		if (tooltip.basicUnleash) {
			Effect unl = new Effect(0,
					"<b> Unleash: </b> Deal X damage to an enemy minion. X equals this minion's magic.") {
				@Override
				public EventFlag unleash() {
					EventFlag ef = new EventFlag(this, false) {
						@Override
						public void resolve(List<Event> eventlist, boolean loopprotection) {
							if (this.effect.unleashTargets.get(0) != null) {
								eventlist.add(new EventEffectDamage(this.effect, this.effect.unleashTargets.get(0),
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
					return c.status == CardStatus.BOARD && c instanceof Minion
							&& ((Minion) c).team != this.getCreator().owner.team;
				}
			};
			LinkedList<Target> list = new LinkedList<Target>();
			list.add(t);
			unl.setUnleashTargets(list);
			this.addEffect(true, unl);
		}
	}

	public Minion realMinion() {
		return (Minion) this.realCard;
	}

	@Override
	public double getValue() {
		// sqrt(atk * hp) + sqrt(magic) + 1
		// TODO make it consider bane, poisonous, shield, etc.
		return Math.sqrt(
				this.finalStatEffects.getStat(EffectStats.ATTACK) * this.finalStatEffects.getStat(EffectStats.HEALTH))
				+ Math.sqrt(this.finalStatEffects.getStat(EffectStats.MAGIC)) + 1;
	}

	public List<Minion> getAttackableTargets() {
		if (this.summoningSickness && (this.finalStatEffects.getStat(EffectStats.STORM) == 0
				&& this.finalStatEffects.getStat(EffectStats.RUSH) == 0)) {
			return new ArrayList<Minion>();
		}
		List<Minion> list = new ArrayList<Minion>();
		List<BoardObject> poss = new ArrayList<BoardObject>();
		poss.addAll(this.board.getBoardObjects(this.team * -1));
		List<Minion> wards = new ArrayList<Minion>();

		Leader enemyLeader = this.board.getPlayer(this.team * -1).leader;
		if (!this.summoningSickness || this.finalStatEffects.getStat(EffectStats.STORM) > 0) {
			list.add(enemyLeader);
		}
		// check for ward
		boolean ward = false;
		for (BoardObject b : poss) {
			if (b instanceof Minion) {
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
		return this.team == this.board.currentPlayerTurn
				&& this.attacksThisTurn < this.finalStatEffects.getStat(EffectStats.ATTACKS_PER_TURN)
				&& (!this.summoningSickness || this.finalStatEffects.getStat(EffectStats.RUSH) > 0
						|| this.finalStatEffects.getStat(EffectStats.STORM) > 0);
	}

	@Override
	public boolean isInPlay() {
		return this.alive && this.health > 0
				&& (this.status.equals(CardStatus.BOARD) || this.status.equals(CardStatus.LEADER));
	}

	public List<EventOnAttack> onAttack(Minion target) {
		List<EventOnAttack> list = new LinkedList<EventOnAttack>();
		for (Effect e : this.getFinalEffects()) {
			EventOnAttack temp = e.onAttack(target); // optimization probably
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<EventOnAttacked> onAttacked(Minion target) {
		List<EventOnAttacked> list = new LinkedList<EventOnAttacked>();
		for (Effect e : this.getFinalEffects()) {
			EventOnAttacked temp = e.onAttacked(target);
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<EventClash> clash(Minion target) {
		List<EventClash> list = new LinkedList<EventClash>();
		for (Effect e : this.getFinalEffects()) {
			EventClash temp = e.clash(target);
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<Event> onDamaged(int damage) {
		List<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onDamaged(damage);
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<EventFlag> unleash() {
		List<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.unleash();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
		// return new LinkedList<Event>();
	}

	public List<Target> getUnleashTargets() {
		List<Target> list = new LinkedList<Target>();
		for (Effect e : this.getFinalEffects()) {
			for (Target t : e.unleashTargets) {
				list.add(t);
			}
		}
		return list;
	}

	@Override
	public String toString() {
		return super.toString() + this.health + " ";
	}
}
