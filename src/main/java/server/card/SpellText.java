package server.card;

import client.tooltip.TooltipSpell;
import server.Board;
import server.card.effect.Effect;
import server.card.effect.EffectStats;

import java.util.ArrayList;
import java.util.List;

public abstract class SpellText extends CardText {
    @Override
    public final List<Effect> getEffects() {
        TooltipSpell tooltip = this.getTooltip();
        Effect e = new Effect("", new EffectStats(tooltip.cost));
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
    public final Spell constructInstance(Board b) {
        return new Spell(b, this);
    }

    protected abstract List<Effect> getSpecialEffects();
    public abstract TooltipSpell getTooltip();
}
