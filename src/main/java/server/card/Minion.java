package server.card;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import client.tooltip.*;
import server.*;
import server.card.effect.*;
import server.card.target.TargetList;
import server.card.target.TargetingScheme;
import server.event.eventgroup.EventGroupType;
import server.resolver.util.ResolverQueue;

public class Minion extends BoardObject {
    public int health, attacksThisTurn = 0; // tempted to make damage an effect
    public boolean summoningSickness = true;

    public Minion(Board board, MinionText minionText) {
        super(board, minionText);
        this.health = this.getTooltip().health;
    }

    public Minion realMinion() {
        return (Minion) this.realCard;
    }

    @Override
    public double getValue(int refs) {
        if (refs < 0) {
            return 0;
        }
        if (this.health < 0) {
            return 0;
        }
        double sum = super.getValue(refs);
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
        sum += (0.9 * Math.sqrt(attack * this.health) + 0.1 * Math.sqrt(magic * Math.pow(this.health, 0.4)) + 1) * super.getPresenceValueMultiplier();
        return sum;
    }

    @Override
    public double getPresenceValueMultiplier() {
        return super.getPresenceValueMultiplier() * (1 - Math.pow(0.5, this.health));
    }

    @Override
    public double getLastWordsValueMultiplier() {
        double a = 0.25, w = 0.75;
        if (this.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
            return a + (1 - a) * (1 - (1 - Math.pow(w, 2 * this.finalStatEffects.getStat(EffectStats.COUNTDOWN))) * (1 - Math.pow(w, this.health)));
        } else {
            return a + (1 - a) * Math.pow(w, this.health);
        }
    }
    // remember to change logic in canAttack(Minion)
    // ideally this is functionally equivalent to minions.filter(this::canAttack)
    // but we do things a bit differently for performance reasons
    public Stream<Minion> getAttackableTargets() {
        if (!this.canAttack()) {
            return Stream.empty();
        }
        Supplier<Stream<Minion>> minions = () -> this.board.getMinions(this.team * -1, false, true);
        // TODO add if can attack this minion eg stealth
        Stream<Minion> attackable = minions.get();

        // TODO add restrictions on can't attack leader
        if (this.attackLeaderConditions()) {
            attackable = Stream.concat(attackable, this.board.getPlayer(this.team * -1).getLeader().stream());
        }

        // love how you can't reuse streams
        if (minions.get().anyMatch(m -> m.finalStatEffects.getStat(EffectStats.WARD) > 0)) {
            return minions.get().filter(m -> m.finalStatEffects.getStat(EffectStats.WARD) > 0);
        }
        return attackable;
    }

    public boolean canAttack() {
        return this.team == this.board.currentPlayerTurn && this.status.equals(CardStatus.BOARD)
                && this.attacksThisTurn < this.finalStatEffects.getStat(EffectStats.ATTACKS_PER_TURN)
                && this.attackMinionConditions();
    }

    // like getAttackableTargets but for a single target
    public boolean canAttack(Minion m) {
        if (!this.canAttack()) {
            return false;
        }
        boolean ward = this.board.getMinions(this.team * -1, true, true)
                .anyMatch(potentialWard -> potentialWard.finalStatEffects.getStat(EffectStats.WARD) > 0);
        // TODO add if can attack this minion eg stealth
        return m.isInPlay() && (!ward || m.finalStatEffects.getStat(EffectStats.WARD) > 0)
                && (m.status.equals(CardStatus.BOARD) ? this.attackMinionConditions() : this.attackLeaderConditions());
    }

    private boolean attackMinionConditions() {
        return !this.summoningSickness || this.finalStatEffects.getStat(EffectStats.STORM) > 0
                || this.finalStatEffects.getStat(EffectStats.RUSH) > 0;
    }

    private boolean attackLeaderConditions() {
        return !this.summoningSickness || this.finalStatEffects.getStat(EffectStats.STORM) > 0;
    }

    public boolean canBeUnleashed() {
        return !(this instanceof Leader) && this.isInPlay();
    }

    public List<List<TargetingScheme<?>>> getUnleashTargetingSchemes() {
        List<List<TargetingScheme<?>>> list = new LinkedList<>();
        this.getFinalEffects(true).forEachOrdered(e -> list.add(e.getUnleashTargetingSchemes()));
        return list;
    }

    public ResolverQueue unleash(List<List<TargetList<?>>> targetsList) {
        return this.getTargetedResolvers(EventGroupType.UNLEASH, List.of(this), targetsList, Effect::unleash, eff -> !eff.removed && ((Minion) eff.owner).isInPlay());
    }

    public ResolverQueue onAttack(Minion target) {
        return this.getResolvers(EventGroupType.ONATTACK, List.of(this, target), e -> e.onAttack(target),
                eff -> !eff.removed && ((Minion) eff.owner).isInPlay() && target.isInPlay());
    }

    public ResolverQueue onAttacked(Minion target) {
        return this.getResolvers(EventGroupType.ONATTACKED, List.of(this, target), e -> e.onAttacked(target),
                eff -> !eff.removed && ((Minion) eff.owner).isInPlay() && target.isInPlay());
    }

    public ResolverQueue clash(Minion target) {
        return this.getResolvers(EventGroupType.CLASH, List.of(this, target), e -> e.clash(target),
                eff -> !eff.removed && ((Minion) eff.owner).isInPlay() && target.isInPlay());
    }

    public ResolverQueue onDamaged(int damage) {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), e -> e.onDamaged(damage),
                eff -> !eff.removed && ((Minion) eff.owner).isInPlay());
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        super.appendStringToBuilder(builder);
        builder.append(this.health).append(" ").append(this.attacksThisTurn).append(" ").append(this.summoningSickness).append(" ");
    }

    @Override
    public TooltipMinion getTooltip() {
        return (TooltipMinion) super.getTooltip();
    }
}
