package server.card.effect.common;

import server.card.CardStatus;
import server.card.effect.EffectStats;
import server.card.effect.EffectWithDependentStats;
import server.card.effect.Stat;

public class EffectSpellboostDiscount extends EffectWithDependentStats {
    public static final String DESCRIPTION = "Costs <b>S</b> less.";
    // required by reflection
    public EffectSpellboostDiscount() {
        super(DESCRIPTION, true, EffectStats.builder().set(Stat.SPELLBOOSTABLE, 1).build());
    }

    @Override
    public EffectStats calculateStats() {
        return EffectStats.builder()
            .set(Stat.SPELLBOOSTABLE, 1)
            .change(Stat.COST, -this.owner.spellboosts)
            .build();
    }

    @Override
    public boolean isActive() {
        return this.owner.status.equals(CardStatus.HAND) || this.owner.status.equals(CardStatus.DECK);
    }
}
