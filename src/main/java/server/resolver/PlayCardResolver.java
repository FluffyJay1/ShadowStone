package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;

public class PlayCardResolver extends Resolver {
    public Player p;
    public Card c;
    int position;
    String battlecryTargets;

    public PlayCardResolver(Player p, Card c, int position, String battlecryTargets) {
        super(false);
        this.p = p;
        this.c = c;
        this.position = position;
        this.battlecryTargets = battlecryTargets;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        if (this.p.canPlayCard(this.c)) {
            /*
            It's convenient to store battlecry targets in a string, however
            if the state of the board changes, the string version can be obsolete
            therefore we set the targets before any state changes happen,
            even if the battlecry occurs afterwards
            */
            Target.setListFromString(this.c.getBattlecryTargets(), b, new StringTokenizer(this.battlecryTargets));
            b.processEvent(rl, el, new EventPlayCard(this.p, this.c, this.position));
            b.processEvent(rl, el,
                    new EventManaChange(this.p, -this.c.finalStatEffects.getStat(EffectStats.COST), false, true));
            if (this.c instanceof BoardObject) {
                b.processEvent(rl, el, new EventPutCard(this.c, CardStatus.BOARD, this.p.team, this.position, null));
            } else {
                b.processEvent(rl, el, new EventDestroy(this.c));
            }
            List<Resolver> battlecryList = c.battlecry();
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
