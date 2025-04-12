package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;
import server.resolver.util.ResolverQueue;

public class MinionAttackResolver extends Resolver {
    final Minion m1;
    final Minion m2;
    final boolean playerOrdered;

    public MinionAttackResolver(Minion m1, Minion m2, boolean playerOrdered) {
        super(false);
        this.m1 = m1;
        this.m2 = m2;
        this.playerOrdered = playerOrdered;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        EventGroup attackOrdered = new EventGroup(EventGroupType.MINIONATTACKORDER, List.of(this.m1, this.m2));
        b.pushEventGroup(attackOrdered);
        ResolverQueue queue = new ResolverQueue();
        b.processEvent(queue, el, new EventMinionAttack(this.m1, this.m2, this.playerOrdered)); // various things are depending on this being first
        this.resolveQueue(b, queue, el, queue);
        queue = this.m1.strike(this.m2);
        this.resolveQueue(b, queue, el, queue);
        if (this.m2 instanceof Leader) {
            queue = this.m1.leaderStrike((Leader) this.m2);
            this.resolveQueue(b, queue, el, queue);
        } else {
            queue = this.m1.minionStrike(this.m2);
            this.resolveQueue(b, queue, el, queue);
            queue = this.m1.clash(this.m2);
            this.resolveQueue(b, queue, el, queue);
        }
        queue = this.m2.retaliate(this.m1);
        this.resolveQueue(b, queue, el, queue);
        if (!(this.m1 instanceof Leader)) {
            queue = this.m2.clash(this.m1);
            this.resolveQueue(b, queue, el, queue);
        }
        if (this.m1.alive && this.m1.isInPlay() && this.m2.alive && this.m2.isInPlay()) {
            List<Card> destroyed = new ArrayList<>(4);
            int damage1 = this.m1.finalStats.get(Stat.ATTACK);
            int damage2 = this.m2.finalStats.get(Stat.ATTACK);
            // gleave
            List<Minion> targets1 = new ArrayList<>(3);
            List<Integer> damages1 = new ArrayList<>(3);
            targets1.add(this.m2);
            damages1.add(damage1);
            if (this.m1.finalStats.get(Stat.CLEAVE) > 0 && this.m2.status.equals(CardStatus.BOARD)) {
                int pos = this.m2.getIndex();
                for (int i = -1; i <= 1; i += 2) {
                    int offsetPos = pos + i;
                    BoardObject adjacent = b.getPlayer(this.m2.team).getPlayArea().get(offsetPos);
                    if (adjacent instanceof Minion) {
                        targets1.add((Minion) adjacent);
                        damages1.add(damage1);
                    }
                }
            }
            b.pushEventGroup(new EventGroup(EventGroupType.MINIONCOMBAT));
            DamageResolver d1 = new DamageResolver(this.m1, targets1, damages1, false, m1.getTooltip().attackAnimation);
            DamageResolver d2 = new DamageResolver(this.m2, List.of(this.m1), List.of(damage2), false, m2.getTooltip().attackAnimation);
            this.resolve(b, rq, el, d1);
            this.resolve(b, rq, el, d2);
            destroyed.addAll(d1.destroyed);
            destroyed.addAll(d2.destroyed);
            b.popEventGroup(); // minion combat
            b.popEventGroup(); // attack order
            if (this.m1.finalStats.get(Stat.BANE) > 0 && this.m2.finalStats.get(Stat.STALWART) == 0
                    && !(this.m2 instanceof Leader) && !destroyed.contains(this.m2)) {
                destroyed.add(this.m2);
            }
            if (this.m2.finalStats.get(Stat.BANE) > 0 && this.m1.finalStats.get(Stat.STALWART) == 0
                    && !(this.m1 instanceof Leader) && !destroyed.contains(this.m1)) {
                destroyed.add(this.m1);
            }
            this.resolve(b, rq, el, new DestroyResolver(destroyed, EventDestroy.Cause.NATURAL));
        } else {
            b.popEventGroup(); // attack order
        }
    }
}
