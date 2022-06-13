package server.resolver;

import java.util.*;
import java.util.stream.Collectors;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class TurnStartResolver extends Resolver {
    final Player p;

    public TurnStartResolver(Player p) {
        super(false);
        this.p = p;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        ResolverQueue buffer = new ResolverQueue();
        b.processEvent(buffer, el, new EventTurnStart(p));
        b.processEvent(buffer, el, new EventManaChange(this.p, 1, false, true));
        b.processEvent(buffer, el, new EventManaChange(this.p, this.p.maxmana + 1, true, false));
        // avoid concurrent modification
        List<BoardObject> ours = this.p.board.getBoardObjects(this.p.team, true, true, true, true).collect(Collectors.toList());
        for (BoardObject bo : ours) {
            // things may happen, this bo might be dead already
            if (bo.isInPlay()) {
                this.resolveQueue(b, buffer, el, bo.onTurnStartAllied());
                if (bo.finalStats.contains(Stat.COUNTDOWN)) {
                    Effect e = new Effect();
                    e.effectStats.change.set(Stat.COUNTDOWN, -1);
                    this.resolve(b, buffer, el, new AddEffectResolver(bo, e));
                }
            }
        }
        List<BoardObject> theirs = this.p.board.getBoardObjects(this.p.team * -1, true, true, true, true).collect(Collectors.toList());
        for (BoardObject bo : theirs) {
            // things may happen, this bo might be dead already
            if (bo.isInPlay()) {
                this.resolveQueue(b, buffer, el, bo.onTurnStartEnemy());
            }
        }
        this.resolveQueue(b, buffer, el, buffer);
        this.resolve(b, rq, el, new DrawResolver(this.p, 1));
    }
}
