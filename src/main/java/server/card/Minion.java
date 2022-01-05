package server.card;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.ai.AI;
import server.card.effect.*;
import server.event.*;
import server.resolver.*;

public class Minion extends BoardObject {
    public int health, attacksThisTurn = 0; // tempted to make damage an effect
    public boolean summoningSickness = true;

    public Minion(Board board, TooltipMinion tooltip) {
        super(board, tooltip);
        this.health = tooltip.health;
        Effect e = new Effect("", new EffectStats(tooltip.cost, tooltip.attack, tooltip.magic, tooltip.health));
        e.effectStats.set.setStat(EffectStats.ATTACKS_PER_TURN, 1);
        this.addEffect(true, e);
        if (tooltip.basicUnleash) {
            Effect unl = new Effect(
                    "<b>Unleash</b>: Deal X damage to an enemy minion. X equals this minion's magic.") {
                @Override
                public Resolver unleash() {
                    Effect effect = this; // anonymous fuckery
                    return new Resolver(false) {
                        @Override
                        public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                            List<Card> targets = effect.unleashTargets.get(0).getTargets();
                            if (targets.size() > 0) {
                                Minion target = (Minion) effect.unleashTargets.get(0).getTargets().get(0);
                                EffectDamageResolver edr = new EffectDamageResolver(effect, List.of(target),
                                        List.of(effect.owner.finalStatEffects.getStat(EffectStats.MAGIC)), true, null);
                                this.resolve(board, rl, el, edr);
                            }
                        }
                    };
                }

                @Override
                public double getPresenceValue() {
                    return AI.VALUE_PER_DAMAGE * this.owner.finalStatEffects.getStat(EffectStats.MAGIC) / 2.;
                }
            };
            Target t = new Target(e, 1, "Deal X damage to an enemy minion. X equals this minion's magic.") {
                @Override
                public boolean canTarget(Card c) {
                    return c.status == CardStatus.BOARD && c instanceof Minion
                            && c.team != this.getCreator().owner.team;
                }
            };
            unl.setUnleashTargets(List.of(t));
            this.addEffect(true, unl);
        }
    }

    public Minion realMinion() {
        return (Minion) this.realCard;
    }

    @Override
    public double getValue() {
        if (this.health < 0) {
            return 0;
        }
        double sum = super.getValue();
        // 0.9 * sqrt(atk * hp) + 0.1 * sqrt(magic * hp^(0.4)) + 1
        int attack = this.finalStatEffects.getStat(EffectStats.ATTACK);
        int magic = this.finalStatEffects.getStat(EffectStats.MAGIC);
        // if bane, add 4 to attack value, if poisonous add 6 if attack > 0, only add the max of these two bonuses
        int bonus = 0;
        if (this.finalStatEffects.getStat(EffectStats.BANE) > 0) {
            bonus = 4;
        }
        if (this.finalStatEffects.getStat(EffectStats.POISONOUS) > 0 && attack > 0) {
            bonus = 6;
        }
        attack += bonus;
        // TODO make it consider shield, etc.
        sum += 0.9 * Math.sqrt(attack * this.health) + 0.1 * Math.sqrt(magic * Math.pow(this.health, 0.4)) + 1;
        return sum;
    }

    // remember to change logic in canAttack(Minion)
    public List<Minion> getAttackableTargets() {
        if (this.summoningSickness && (this.finalStatEffects.getStat(EffectStats.STORM) == 0
                && this.finalStatEffects.getStat(EffectStats.RUSH) == 0)) {
            return new ArrayList<>();
        }
        List<Minion> list = new ArrayList<>();
        List<Minion> poss = new ArrayList<>(this.board.getMinions(this.team * -1, false, true));
        List<Minion> wards = new ArrayList<>();

        Leader enemyLeader = this.board.getPlayer(this.team * -1).getLeader();
        if (!this.summoningSickness || this.finalStatEffects.getStat(EffectStats.STORM) > 0) {
            list.add(enemyLeader);
        }
        // check for ward
        boolean ward = false;
        for (Minion m : poss) {
            if (!this.summoningSickness || this.finalStatEffects.getStat(EffectStats.RUSH) > 0
                    || this.finalStatEffects.getStat(EffectStats.STORM) > 0) {
                // TODO add if can attack this minion eg stealth or can't be
                // attacked
                list.add(m);
            }
            if (m.finalStatEffects.getStat(EffectStats.WARD) > 0) {
                ward = true;
                wards.add(m);
            }
            // TODO add restrictions on can't attack leader
        }
        if (ward) {
            return wards;
        }
        return list;
    }

    public boolean canAttack() {
        return this.team == this.board.currentPlayerTurn && this.status.equals(CardStatus.BOARD)
                && this.attacksThisTurn < this.finalStatEffects.getStat(EffectStats.ATTACKS_PER_TURN)
                && (!this.summoningSickness || this.finalStatEffects.getStat(EffectStats.RUSH) > 0
                        || this.finalStatEffects.getStat(EffectStats.STORM) > 0);
    }

    // like getAttackableTargets but for a single target
    public boolean canAttack(Minion m) {
        if (!this.canAttack()) {
            return false;
        }
        boolean ward = false;
        for (Minion potentialWard : this.board.getMinions(this.team * -1, true, true)) {
            if (potentialWard.finalStatEffects.getStat(EffectStats.WARD) > 0) {
                ward = true;
                break;
            }
        }
        return (!ward || m.finalStatEffects.getStat(EffectStats.WARD) > 0)
                && (!this.summoningSickness || (this.finalStatEffects.getStat(EffectStats.STORM) > 0
                        || (this.finalStatEffects.getStat(EffectStats.RUSH) > 0 && m.status.equals(CardStatus.BOARD))));
    }

    public boolean canBeUnleashed() {
        return !(this instanceof Leader) && this.isInPlay();
    }

    public List<Target> getUnleashTargets() {
        List<Target> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            list.addAll(e.unleashTargets);
        }
        return list;
    }

    @Override
    public String toString() {
        return super.toString() + this.health + " " + this.attacksThisTurn + " " + this.summoningSickness + " ";
    }

    @Override
    public TooltipMinion getTooltip() {
        return (TooltipMinion) super.getTooltip();
    }
}
