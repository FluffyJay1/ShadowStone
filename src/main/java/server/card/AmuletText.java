package server.card;

import client.tooltip.TooltipAmulet;
import server.Board;
import server.BoardObjectText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

import java.util.ArrayList;
import java.util.List;

public abstract class AmuletText extends BoardObjectText {
    @Override
    public final List<Effect> getEffects() {
        TooltipAmulet tooltip = this.getTooltip();
        EffectStats stats = new EffectStats(tooltip.cost);
        stats.traits.addAll(tooltip.traits);
        Effect e = new Effect("", stats);
        List<Effect> special = this.getSpecialEffects();
        int specialSize = 0;
        if (special != null) {
            specialSize = special.size();
        }
        List<Effect> ret = new ArrayList<>(specialSize + 1);
        ret.add(e);
        if (special != null) {
            ret.addAll(special);
        }
        return ret;
    }

    @Override
    public final Amulet constructInstance(Board b) {
        return new Amulet(b, this);
    }

    protected abstract List<Effect> getSpecialEffects();
    public abstract TooltipAmulet getTooltip();
}
