package server;

import server.ai.AI;
import server.card.*;
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
                eff -> !eff.removed && eff.owner.isInPlay() && target.isInPlay());
    }

    public ResolverQueue onUnleashPost(Minion target) {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), eff -> eff.onUnleashPost(target),
                eff -> !eff.removed && eff.owner.isInPlay() && target.isInPlay());
    }

    @Override
    public double getValue(int refs) {
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
