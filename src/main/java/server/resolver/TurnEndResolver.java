package server.resolver;

import java.util.*;
import java.util.stream.Collectors;

import server.*;
import server.card.*;
import server.card.effect.EffectUntilTurnEnd;
import server.card.effect.EffectUntilTurnEndAllied;
import server.card.effect.EffectUntilTurnEndEnemy;
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
        List<EffectUntilTurnEnd> relevantEffectsToRemove = b.effectsToRemoveAtEndOfTurn.stream()
                .filter(eff -> eff.owner.team == this.p.team ? !(eff instanceof EffectUntilTurnEndEnemy) : !(eff instanceof EffectUntilTurnEndAllied))
                .collect(Collectors.toList());
        this.resolve(b, subList, el, new RemoveEffectResolver(relevantEffectsToRemove));
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
    }

}
