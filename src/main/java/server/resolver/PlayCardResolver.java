package server.resolver;

import java.util.*;

import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.card.target.TargetList;
import server.event.*;
import server.resolver.util.ResolverQueue;

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
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        if (this.p.canPlayCard(this.c)) {
            ResolverQueue battlecry = c.battlecry(this.battlecryTargets); // save the resolvers before doing anything, just in case stuff happens
            b.processEvent(rq, el, new EventPlayCard(this.p, this.c, this.position));
            b.processEvent(rq, el,
                    new EventManaChange(this.p, -this.c.finalStats.get(Stat.COST), true, false, false));
            if (this.c instanceof BoardObject) {
                b.processEvent(rq, el, new EventPutCard(List.of(this.c), CardStatus.BOARD, this.p.team, List.of(this.position), true, null, EventAnimation.stringOrNull(null)));
            } else {
                // a spell
                b.processEvent(rq, el, new EventDestroy(List.of(this.c), EventDestroy.Cause.CAST)); // do not increment shadows yet, will throw off necromancy resolvers
                this.resolve(b, rq, el, new SpellboostResolver(this.p.getHand()));
            }
            this.resolveQueue(b, rq, el, battlecry);
            if (this.c instanceof Spell) {
                // now we can increment shadows
                b.processEvent(rq, el, new EventGainShadow(this.c.player, 1));
            }
        }
    }
}
