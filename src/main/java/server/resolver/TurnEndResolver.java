package server.resolver;

import java.util.*;
import java.util.stream.Collectors;

import server.*;
import server.card.*;
import server.card.effect.Effect;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;

public class TurnEndResolver extends Resolver {
    final Player p;

    public TurnEndResolver(Player p) {
        super(false);
        this.p = p;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        b.processEvent(rl, el, new EventTurnEnd(p));
        List<Resolver> subList = new LinkedList<>();
        // avoid concurrent modification
        List<BoardObject> ours = this.p.board.getBoardObjects(this.p.team, true, true, true, true).collect(Collectors.toList());
        for (BoardObject bo : ours) {
            // things may happen, this bo might be dead already
            if (bo.isInPlay()) {
                b.pushEventGroup(new EventGroup(EventGroupType.FLAG, List.of(bo)));
                this.resolveList(b, subList, el, bo.getResolvers(Effect::onTurnEnd));
                b.popEventGroup();
            }
        }
        List<BoardObject> theirs = this.p.board.getBoardObjects(this.p.team * -1, true, true, true, true).collect(Collectors.toList());
        for (BoardObject bo : theirs) {
            // things may happen, this bo might be dead already
            if (bo.isInPlay()) {
                b.pushEventGroup(new EventGroup(EventGroupType.FLAG, List.of(bo)));
                this.resolveList(b, subList, el, bo.getResolvers(Effect::onTurnEndEnemy));
                b.popEventGroup();
            }
        }
        this.resolveList(b, subList, el, subList);
    }

}
