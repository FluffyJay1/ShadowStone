package server.card;

import client.tooltip.TooltipMinion;
import server.Board;
import server.card.effect.Effect;

import java.util.List;

public abstract class LeaderText extends MinionText {
    @Override
    public final Leader constructInstance(Board b) {
        return new Leader(b, this);
    }

    protected abstract List<Effect> getSpecialEffects();
    public abstract TooltipMinion getTooltip();
}
