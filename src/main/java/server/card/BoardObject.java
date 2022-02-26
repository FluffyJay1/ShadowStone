package server.card;

import client.tooltip.*;
import server.*;
import server.card.effect.*;
import server.resolver.Resolver;
import utils.HistoricalList;

import java.util.List;

public class BoardObject extends Card {
    public int lastBoardPos = 0; // After leaving board (e.g. to graveyard), keep a record of where it was last
    public int lastBoardEpoch = 0; // see HistoricalList.java

    public BoardObject(Board b, CardText cardText) {
        super(b, cardText);
    }

    public boolean isInPlay() {
        return this.status.equals(CardStatus.BOARD);
    }

    /**
     * Get current board pos if on the board, or the last board pos (translated
     * to current times) if not on board currently
     *
     * @return The position on board
     */
    public int getRelevantBoardPos() {
        if (this.status.equals(CardStatus.BOARD)) {
            return this.getIndex();
        }
        HistoricalList<BoardObject> playArea = this.board.getPlayer(this.team).getPlayArea();
        return playArea.forecastPosition(this.lastBoardPos, this.lastBoardEpoch);
    }

    @Override
    public double getValue(int refs) {
        if (refs < 0) {
            return 0;
        }
        double sum = this.getTotalEffectValueOf(e -> e.getPresenceValue(refs)) * this.getPresenceValueMultiplier();
        sum += this.getTotalEffectValueOf(e -> e.getLastWordsValue(refs)) * this.getLastWordsValueMultiplier();
        if (this.status.equals(CardStatus.HAND)) {
            sum += this.getTotalEffectValueOf(e -> e.getBattlecryValue(refs));
        }
        return sum;
    }

    // having the countdown effect means u'll die in a few turns
    public double getPresenceValueMultiplier() {
        if (this.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
            return 1 - Math.pow(0.5, this.finalStatEffects.getStat(EffectStats.COUNTDOWN));
        } else {
            return 1;
        }
    }

    // the closer we are to deth, the closer we are to activating last words
    public double getLastWordsValueMultiplier() {
        double a = 0.25, w = 0.75;
        if (this.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
            return a + (1 - a) * Math.pow(w, 2 * this.finalStatEffects.getStat(EffectStats.COUNTDOWN));
        } else {
            return a;
        }
    }

    @Override
    public List<Resolver> battlecry() {
        return this.getResolvers(Effect::battlecry, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public List<Resolver> onTurnStart() {
        return this.getResolvers(Effect::onTurnStart, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public List<Resolver> onTurnEnd() {
        return this.getResolvers(Effect::onTurnEnd, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public List<Resolver> onTurnStartEnemy() {
        return this.getResolvers(Effect::onTurnStartEnemy, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public List<Resolver> onTurnEndEnemy() {
        return this.getResolvers(Effect::onTurnEndEnemy, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public List<Resolver> lastWords() {
        return this.getResolvers(Effect::lastWords);
    }

    public List<Resolver> onEnterPlay() {
        return this.getResolvers(Effect::onEnterPlay, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public List<Resolver> onLeavePlay() {
        return this.getResolvers(Effect::onLeavePlay, eff -> !eff.removed && !((BoardObject) eff.owner).isInPlay());
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        super.appendStringToBuilder(builder);
        builder.append(this.lastBoardPos).append(" ").append(this.lastBoardEpoch).append(" ");
    }
}
