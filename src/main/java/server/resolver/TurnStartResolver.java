package server.resolver;

import java.util.*;
import java.util.stream.Collectors;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.resolver.util.ResolverQueue;

public class TurnStartResolver extends Resolver {
    final Player p;

    public TurnStartResolver(Player p) {
        super(false);
        this.p = p;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        b.processEvent(rq, el, new EventTurnStart(p));
        b.processEvent(rq, el, new EventManaChange(this.p, 1, true, false));
        b.processEvent(rq, el, new EventManaChange(this.p, this.p.maxmana + 1, false, true));
        // avoid concurrent modification
        List<BoardObject> ours = this.p.board.getBoardObjects(this.p.team, true, true, true, true).collect(Collectors.toList());
        for (BoardObject bo : ours) {
            // things may happen, this bo might be dead already
            if (bo.isInPlay()) {
                b.pushEventGroup(new EventGroup(EventGroupType.FLAG, List.of(bo)));
                this.resolveQueue(b, rq, el, bo.onTurnStart());
                b.popEventGroup();
                if (bo.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
                    Effect e = new Effect();
                    e.effectStats.change.setStat(EffectStats.COUNTDOWN, -1);
                    this.resolve(b, rq, el, new AddEffectResolver(bo, e));
                }
            }
        }
        List<BoardObject> theirs = this.p.board.getBoardObjects(this.p.team * -1, true, true, true, true).collect(Collectors.toList());
        for (BoardObject bo : theirs) {
            // things may happen, this bo might be dead already
            if (bo.isInPlay()) {
                b.pushEventGroup(new EventGroup(EventGroupType.FLAG, List.of(bo)));
                this.resolveQueue(b, rq, el, bo.onTurnStartEnemy());
                b.popEventGroup();
            }
        }
        this.resolve(b, rq, el, new DrawResolver(this.p, 1));
    }
}
