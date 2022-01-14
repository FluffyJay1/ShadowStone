package server.card;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.effect.*;
import server.resolver.*;

public class BoardObject extends Card {
    public int lastBoardPos = 0; // After leaving board (e.g. to graveyard), keep a record of where it was last

    public BoardObject(Board b, TooltipCard tooltip) {
        super(b, tooltip);
    }

    public boolean isInPlay() {
        return this.status.equals(CardStatus.BOARD);
    }

    @Override
    public double getValue() {
        double sum = this.getTotalEffectValueOf(Effect::getPresenceValue);
        if (this.status.equals(CardStatus.HAND)) {
            sum += this.getTotalEffectValueOf(Effect::getBattlecryValue);
        }
        return sum;
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        super.appendStringToBuilder(builder);
        builder.append(this.lastBoardPos).append(" ");
    }
}
