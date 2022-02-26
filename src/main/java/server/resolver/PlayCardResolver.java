package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.card.target.TargetList;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;

public class PlayCardResolver extends Resolver {
    public final Player p;
    public final Card c;
    final int position;
    final List<List<TargetList<?>>> battlecryTargets;

    public PlayCardResolver(Player p, Card c, int position, List<List<TargetList<?>>> battlecryTargets) {
        super(false);
        this.p = p;
        this.c = c;
        this.position = position;
        this.battlecryTargets = battlecryTargets;
    }

    @Override
    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
        if (this.p.canPlayCard(this.c)) {
            c.setBattlecryTargets(this.battlecryTargets);
            b.processEvent(rl, el, new EventPlayCard(this.p, this.c, this.position));
            b.processEvent(rl, el,
                    new EventManaChange(this.p, -this.c.finalStatEffects.getStat(EffectStats.COST), false, true));
            if (this.c instanceof BoardObject) {
                b.processEvent(rl, el, new EventPutCard(List.of(this.c), CardStatus.BOARD, this.p.team, List.of(this.position), null));
            } else {
                b.processEvent(rl, el, new EventDestroy(this.c));
            }
            List<Resolver> battlecryList = this.c.battlecry();
            if (!(this.c instanceof Spell)) {
                b.pushEventGroup(new EventGroup(EventGroupType.BATTLECRY, List.of(this.c)));
            }
            this.resolveList(b, rl, el, battlecryList);
            if (!(this.c instanceof Spell)) {
                b.popEventGroup();
            }
        }
    }
}
