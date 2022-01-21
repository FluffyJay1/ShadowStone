package server.card;

import client.tooltip.*;
import server.*;
import server.card.effect.*;

public class Spell extends Card { // yea

    public Spell(Board board, TooltipSpell tooltip) {
        super(board, tooltip);
        this.addEffect(true, new Effect("", new EffectStats(tooltip.cost)));
    }

    @Override
    public double getValue(int refs) {
        if (refs < 0) {
            return 0;
        }
        return this.getTotalEffectValueOf(e -> e.getBattlecryValue(refs));
    }

    @Override
    public TooltipSpell getTooltip() {
        return (TooltipSpell) super.getTooltip();
    }
}
