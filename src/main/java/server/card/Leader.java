package server.card;

import client.tooltip.TooltipLeader;
import server.*;

public class Leader extends Minion {
    public Leader(Board b, LeaderText leaderText) {
        super(b, leaderText);
        this.summoningSickness = false; // to make leaders unfreeze
    }

    @Override
    public boolean isInPlay() {
        return this.status.equals(CardStatus.LEADER);
    }

    @Override
    public TooltipLeader getTooltip() {
        return (TooltipLeader) super.getTooltip();
    }

    @Override
    public LeaderText getCardText() {
        return (LeaderText) super.getCardText();
    }
}
