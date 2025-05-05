package server.resolver;

import java.util.*;
import java.util.stream.Collectors;

import server.*;
import server.card.*;
import server.card.effect.Effect;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class TurnEndResolver extends Resolver {
    final Player p;

    public TurnEndResolver(Player p) {
        super(false);
        this.p = p;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        b.processEvent(rq, el, new EventTurnEnd(p));
        ResolverQueue subList = new ResolverQueue();
        // avoid concurrent modification
        List<BoardObject> ours = this.p.board.getBoardObjects(this.p.team, true, true, true, true).collect(Collectors.toList());
        for (BoardObject bo : ours) {
            // things may happen, this bo might be dead already
            if (bo.isInPlay()) {
                this.resolveQueue(b, subList, el, bo.onTurnEndAllied());
            }
        }
        List<BoardObject> theirs = this.p.board.getBoardObjects(this.p.team * -1, true, true, true, true).collect(Collectors.toList());
        for (BoardObject bo : theirs) {
            // things may happen, this bo might be dead already
            if (bo.isInPlay()) {
                this.resolveQueue(b, subList, el, bo.onTurnEndEnemy());
            }
        }
        this.resolveQueue(b, subList, el, subList);
        List<Effect> relevantEffectsToTick = b.effectsToRemoveAtEndOfTurn.stream()
                .filter(eff -> eff.owner.team * eff.untilTurnEndTeam * -1 != this.p.team && eff.untilTurnEndCount != null)
                .toList();
        b.processEvent(rq, el, new EventDecrementEffectTurnEndCount(relevantEffectsToTick));
        List<Effect> relevantEffectsToRemove = b.effectsToRemoveAtEndOfTurn.stream()
                .filter(eff -> eff.owner.team * eff.untilTurnEndTeam * -1 != this.p.team && (eff.untilTurnEndCount == null || eff.untilTurnEndCount.intValue() <= 0))
                .toList();
        this.resolve(b, subList, el, new RemoveEffectResolver(relevantEffectsToRemove)); // remove at the end to allow ephemeral effects to trigger their onturnend on last time
        this.resolveQueue(b, subList, el, subList);
    }

}
