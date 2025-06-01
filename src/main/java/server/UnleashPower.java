package server;

import server.ai.AI;
import server.card.*;
import server.card.effect.Stat;
import server.event.eventgroup.EventGroupType;
import server.resolver.util.ResolverQueue;

import java.util.List;

public class UnleashPower extends Card {
    public int unleashesThisTurn = 0;

    public UnleashPower(Board b, UnleashPowerText unleashPowerText) {
        super(b, unleashPowerText);
    }

    public ResolverQueue onUnleashPre(Minion target) {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), eff -> eff.onUnleashPre(target),
                eff -> !eff.removed && !eff.mute && eff.owner.isInPlay() && target.isInPlay());
    }

    public ResolverQueue onUnleashPost(Minion target) {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), eff -> eff.onUnleashPost(target),
                eff -> !eff.removed && !eff.mute && eff.owner.isInPlay() && target.isInPlay());
    }

    public boolean canUnleash() {
        return this.team == this.board.getCurrentPlayerTurn() 
                && this.status.equals(CardStatus.UNLEASHPOWER)
                && this.unleashesThisTurn < this.finalStats.get(Stat.ATTACKS_PER_TURN)
                && this.canUnleashEventually();
    }

    public boolean canUnleashEventually() {
        return this.finalStats.get(Stat.DISARMED) == 0 
                && this.finalStats.get(Stat.FROZEN) == 0
                && this.board.getPlayer(this.team).unleashAllowed;
    }

    public boolean shouldBeUnfrozen() {
        if (this.finalStats.get(Stat.FROZEN) == 0 || this.unleashesThisTurn >= this.finalStats.get(Stat.ATTACKS_PER_TURN)
                || this.finalStats.get(Stat.DISARMED) > 0) {
            return false;
        }
        return this.board.getPlayer(this.team).unleashAllowed;
    }

    @Override
    public double getValue(int refs) {
        if (!this.canUnleashEventually()) {
            return 0;
        }
        return AI.VALUE_OF_UNLEASH;
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        super.appendStringToBuilder(builder);
        builder.append(this.unleashesThisTurn).append(" ");
    }

    @Override
    public boolean isInPlay() {
        return this.status.equals(CardStatus.UNLEASHPOWER);
    }
}
