package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

public class TurnEndResolver extends Resolver {
    Player p;

    public TurnEndResolver(Player p) {
        super(false);
        this.p = p;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        b.processEvent(rl, el, new EventTurnEnd(p));
        List<Resolver> subList = new LinkedList<>();
        for (BoardObject bo : this.p.board.getBoardObjects(this.p.team, true, true, true)) {
            this.resolveList(b, subList, el, bo.onTurnEnd());
        }
        for (BoardObject bo : this.p.board.getBoardObjects(this.p.team * -1, true, true, true)) {
            this.resolveList(b, subList, el, bo.onTurnEndEnemy());
        }
        this.resolveList(b, subList, el, subList);
    }

}
