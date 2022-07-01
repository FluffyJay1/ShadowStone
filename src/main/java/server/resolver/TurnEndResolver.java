package server.resolver;

import java.util.*;
import java.util.stream.Collectors;

import server.*;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;
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
        List<Effect> relevantEffectsToRemove = b.effectsToRemoveAtEndOfTurn.stream()
                .filter(eff -> eff.owner.team * eff.untilTurnEndTeam * -1 != this.p.team)
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
