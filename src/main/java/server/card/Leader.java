package server.card;

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
}
