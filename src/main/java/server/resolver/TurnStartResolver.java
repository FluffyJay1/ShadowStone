package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class TurnStartResolver extends Resolver {
    Player p;

    public TurnStartResolver(Player p) {
        super(false);
        this.p = p;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        b.processEvent(rl, el, new EventTurnStart(p));
        b.processEvent(rl, el, new EventManaChange(this.p, 1, true, false));
        b.processEvent(rl, el, new EventManaChange(this.p, this.p.maxmana + 1, false, true));
        for (BoardObject bo : this.p.board.getBoardObjects(this.p.team, true, true, true)) {
            this.resolveList(b, rl, el, bo.onTurnStart());
            if (bo.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
                Effect e = new Effect();
                e.change.setStat(EffectStats.COUNTDOWN, -1);
                this.resolve(b, rl, el, new AddEffectResolver(bo, e));
            }
        }
        for (BoardObject bo : this.p.board.getBoardObjects(this.p.team * -1, true, true, true)) {
            this.resolveList(b, rl, el, bo.onTurnStartEnemy());
        }
        this.resolve(b, rl, el, new DrawResolver(this.p, 1));
    }
}