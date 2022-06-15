package server.card.effect.common;

import server.card.CardStatus;
import server.card.effect.EffectStats;
import server.card.effect.EffectWithDependentStats;
import server.card.effect.Stat;

public class EffectSpellboostDiscount extends EffectWithDependentStats {
    public static final String DESCRIPTION = "Costs 1 less for each time this card has been <b>Spellboosted</b>.";
    // required by reflection
    public EffectSpellboostDiscount() {
        super(DESCRIPTION, true);
        this.effectStats.set.set(Stat.SPELLBOOSTABLE, 1);
    }

    @Override
    public EffectStats calculateStats() {
        EffectStats ret = new EffectStats();
        ret.set.set(Stat.SPELLBOOSTABLE, 1);
        ret.change.set(Stat.COST, -this.owner.spellboosts);
        return ret;
    }

    @Override
    public boolean isActive() {
        return this.owner.status.equals(CardStatus.HAND) || this.owner.status.equals(CardStatus.DECK);
    }
}
