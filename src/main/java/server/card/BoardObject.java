package server.card;

import server.*;
import server.ai.AI;
import server.card.effect.*;
import server.card.target.TargetList;
import server.event.eventgroup.EventGroupType;
import server.resolver.util.ResolverQueue;
import utils.HistoricalList;

import java.util.List;

public class BoardObject extends Card {
    public int lastBoardPos = 0; // After leaving board (e.g. to graveyard), keep a record of where it was last
    public int lastBoardEpoch = 0; // see HistoricalList.java

    public BoardObject(Board b, CardText cardText) {
        super(b, cardText);
    }

    @Override
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
        double sum = super.getValue(refs);
        sum += this.getTotalEffectValueOf(e -> e.getPresenceValue(refs)) * this.getPresenceValueMultiplier();
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
    public ResolverQueue battlecry(List<List<TargetList<?>>> targetsList) {
        return this.getTargetedResolvers(EventGroupType.BATTLECRY, List.of(this), targetsList, Effect::battlecry, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public ResolverQueue onTurnStart() {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), Effect::onTurnStart, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public ResolverQueue onTurnEnd() {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), Effect::onTurnEnd, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public ResolverQueue onTurnStartEnemy() {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), Effect::onTurnStartEnemy, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public ResolverQueue onTurnEndEnemy() {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), Effect::onTurnEndEnemy, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public ResolverQueue lastWords() {
        return this.getResolvers(EventGroupType.LASTWORDS, List.of(this), Effect::lastWords, eff -> true);
    }

    public ResolverQueue onEnterPlay() {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), Effect::onEnterPlay, eff -> !eff.removed && ((BoardObject) eff.owner).isInPlay());
    }

    public ResolverQueue onLeavePlay() {
        return this.getResolvers(EventGroupType.FLAG, List.of(this), Effect::onLeavePlay, eff -> !eff.removed && !((BoardObject) eff.owner).isInPlay());
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        super.appendStringToBuilder(builder);
        builder.append(this.lastBoardPos).append(" ").append(this.lastBoardEpoch).append(" ");
    }
}
