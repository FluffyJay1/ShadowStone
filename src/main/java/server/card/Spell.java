package server.card;

import client.tooltip.*;
import server.*;

public class Spell extends Card { // yea

    public Spell(Board board, SpellText text) {
        super(board, text);
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
