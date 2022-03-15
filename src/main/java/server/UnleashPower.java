package server;

import server.*;
import server.card.*;
import server.resolver.util.ResolverQueue;

public class UnleashPower extends Card {
    public int unleashesThisTurn = 0;

    public UnleashPower(Board b, UnleashPowerText unleashPowerText) {
        super(b, unleashPowerText);
    }

    public ResolverQueue onUnleashPre(Minion target) {
        return this.getResolvers(e -> e.onUnleashPre(target), eff -> !eff.removed && ((UnleashPower) eff.owner).isInPlay() && target.isInPlay());
    }

    public ResolverQueue onUnleashPost(Minion target) {
        return this.getResolvers(e -> e.onUnleashPost(target), eff -> !eff.removed && ((UnleashPower) eff.owner).isInPlay() && target.isInPlay());
    }

    @Override
    public double getValue(int refs) {
        return 0;
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        super.appendStringToBuilder(builder);
        builder.append(this.unleashesThisTurn).append(" ");
    }

    public boolean isInPlay() {
        return this.status.equals(CardStatus.UNLEASHPOWER);
    }
}
