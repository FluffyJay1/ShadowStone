package server.card;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import client.tooltip.*;
import server.*;
import server.ai.AI;
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

    private double getEffectiveHealth() {
        double health = this.health;
        if (this.finalStats.get(Stat.SHIELD) > 0) {
            // existence of shield has some value on its own
            // let's say average 2 dmg overflow
            health += this.finalStats.get(Stat.SHIELD) + 2;
        }
        return health;
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
        int attack = this.finalStats.get(Stat.ATTACK);
        int magic = this.finalStats.get(Stat.MAGIC);
        if (this.finalStats.get(Stat.LIFESTEAL) > 0) {
            sum += attack * AI.VALUE_PER_HEAL;
        }
        // if bane, add 4 to attack value, if poisonous add 6 if attack > 0, only add the max of these two bonuses
        int bonus = 0;
        if (this.finalStats.get(Stat.BANE) > 0) {
            bonus = 4;
        }
        if (this.finalStats.get(Stat.POISONOUS) > 0 && attack > 0) {
            bonus = 6;
        }
        attack += bonus;
        // if disarmed, then attack isn't quite as useful
        if (this.finalStats.get(Stat.DISARMED) > 0 || this.finalStats.get(Stat.FROZEN) > 0) {
            attack /= 2;
        }
        double effectiveHealth = this.getEffectiveHealth();
        // TODO make it consider other stats
        sum += (0.9 * Math.sqrt(attack * effectiveHealth) + 0.1 * Math.sqrt(magic * Math.pow(effectiveHealth, 0.4)) + 1) * super.getPresenceValueMultiplier();
        return sum;
    }

    @Override
    public double getPresenceValueMultiplier() {
        return super.getPresenceValueMultiplier() * (1 - Math.pow(0.5, this.getEffectiveHealth()));
    }

    @Override
    public double getLastWordsValueMultiplier() {
        double a = 0.25, w = 0.75;
        double effectiveHealth = this.getEffectiveHealth();
        if (this.finalStats.contains(Stat.COUNTDOWN)) {
            return a + (1 - a) * (1 - (1 - Math.pow(w, 2 * this.finalStats.get(Stat.COUNTDOWN))) * (1 - Math.pow(w, effectiveHealth)));
        } else {
            return a + (1 - a) * Math.pow(w, effectiveHealth);
        }
    }

    public boolean canBeAttacked() {
        return this.finalStats.get(Stat.STEALTH) == 0;
    }
    // remember to change logic in canAttack(Minion) and shouldBeUnfrozen()
    // ideally this is functionally equivalent to minions.filter(this::canAttack)
    // but we do things a bit differently for performance reasons
    public Stream<Minion> getAttackableTargets() {
        if (!this.canAttack()) {
            return Stream.empty();
        }
        Supplier<Stream<Minion>> minions = () -> this.board.getMinions(this.team * -1, false, true)
                .filter(Minion::canBeAttacked);
        Stream<Minion> attackable = minions.get();

        // TODO add restrictions on can't attack leader
        if (this.attackLeaderConditions()) {
            attackable = Stream.concat(attackable, this.board.getPlayer(this.team * -1).getLeader().stream().filter(Minion::canBeAttacked));
        }

        // love how you can't reuse streams
        if (minions.get().anyMatch(m -> m.finalStats.get(Stat.WARD) > 0)) {
            return minions.get().filter(m -> m.finalStats.get(Stat.WARD) > 0);
        }
        return attackable;
    }

    public boolean shouldBeUnfrozen() {
        if (this.finalStats.get(Stat.FROZEN) == 0 || this.attacksThisTurn == this.finalStats.get(Stat.ATTACKS_PER_TURN)
                || this.finalStats.get(Stat.DISARMED) > 0
                || (this.summoningSickness && this.finalStats.get(Stat.STORM) == 0 && this.finalStats.get(Stat.RUSH) == 0)) {
            return false;
        }
        if (this.attackMinionConditions() && this.board.getMinions(this.team * -1, false, true)
                .anyMatch(Minion::canBeAttacked)) {
            return true;
        }
        Optional<Leader> ol = this.board.getPlayer(this.team * -1).getLeader();
        return this.attackLeaderConditions() && ol.isPresent() && ol.get().canBeAttacked();
    }

    public boolean canAttack() {
        return this.team == this.board.getCurrentPlayerTurn() && this.status.equals(CardStatus.BOARD)
                && this.attacksThisTurn < this.finalStats.get(Stat.ATTACKS_PER_TURN)
                && this.finalStats.get(Stat.DISARMED) == 0 && this.finalStats.get(Stat.FROZEN) == 0
                && this.attackMinionConditions();
    }

    // like getAttackableTargets but for a single target
    public boolean canAttack(Minion m) {
        if (!this.canAttack()) {
            return false;
        }
        boolean ward = this.board.getMinions(this.team * -1, true, true)
                .anyMatch(potentialWard -> potentialWard.finalStats.get(Stat.STEALTH) == 0
                        && potentialWard.finalStats.get(Stat.WARD) > 0);
        return m.isInPlay() && (!ward || m.finalStats.get(Stat.WARD) > 0) && m.canBeAttacked()
                && (m.status.equals(CardStatus.BOARD) ? this.attackMinionConditions() : this.attackLeaderConditions());
    }

    private boolean attackMinionConditions() {
        return !this.summoningSickness || this.finalStats.get(Stat.STORM) > 0
                || this.finalStats.get(Stat.RUSH) > 0;
    }

    private boolean attackLeaderConditions() {
        return (!this.summoningSickness || this.finalStats.get(Stat.STORM) > 0) && this.finalStats.get(Stat.CANT_ATTACK_LEADER) == 0;
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
        return this.getTargetedResolvers(EventGroupType.UNLEASH, List.of(this), targetsList, Effect::unleash, eff -> !eff.removed && !eff.mute && ((Minion) eff.owner).isInPlay());
    }

    public ResolverQueue strike(Minion target) {
        return this.getResolvers(EventGroupType.ONATTACK, List.of(this, target), e -> e.strike(target),
                eff -> !eff.removed && !eff.mute && eff.owner.isInPlay() && target.isInPlay());
    }

    public ResolverQueue minionStrike(Minion target) {
        return this.getResolvers(EventGroupType.ONATTACK, List.of(this, target), e -> e.minionStrike(target),
                eff -> !eff.removed && !eff.mute && eff.owner.isInPlay() && target.isInPlay());
    }

    public ResolverQueue leaderStrike(Leader target) {
        return this.getResolvers(EventGroupType.ONATTACK, List.of(this, target), e -> e.leaderStrike(target),
                eff -> !eff.removed && !eff.mute && eff.owner.isInPlay() && target.isInPlay());
    }

    public ResolverQueue retaliate(Minion target) {
        return this.getResolvers(EventGroupType.ONATTACKED, List.of(this, target), e -> e.retaliate(target),
                eff -> !eff.removed && !eff.mute && eff.owner.isInPlay() && target.isInPlay());
    }

    public ResolverQueue clash(Minion target) {
        return this.getResolvers(EventGroupType.CLASH, List.of(this, target), e -> e.clash(target),
                eff -> !eff.removed && !eff.mute && eff.owner.isInPlay() && target.isInPlay());
    }

    public ResolverQueue onDamaged(int damage) {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), e -> e.onDamaged(damage),
                eff -> !eff.removed && !eff.mute && eff.owner.isInPlay());
    }

    public boolean unleashSpecialConditions() {
        return this.getFinalEffects(true).anyMatch(Effect::unleashSpecialConditions);
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

    @Override
    public MinionText getCardText() {
        return (MinionText) super.getCardText();
    }
}
