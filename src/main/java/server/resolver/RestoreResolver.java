package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class RestoreResolver extends Resolver {
    public final List<Integer> heal;
    public List<Integer> actualHeal;
    public final List<Minion> m;
    public final Effect source;

    public RestoreResolver(Effect source, List<Minion> m, List<Integer> heal) {
        super(false);
        this.source = source;
        this.m = m;
        this.heal = heal;
    }

    public RestoreResolver(Effect source, Minion m, int heal) {
        this(source, List.of(m), List.of(heal));
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        EventRestore er = b.processEvent(rl, el, new EventRestore(this.source, this.m, this.heal));
        this.actualHeal = er.actualHeal;
    }

}
