package server.card;

import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.effect.Effect;

import java.util.List;

public class Leader extends Minion {
    public Leader(Board b, LeaderText leaderText) {
        super(b, leaderText);
    }

    @Override
    public boolean isInPlay() {
        return this.status.equals(CardStatus.LEADER);
    }
}
