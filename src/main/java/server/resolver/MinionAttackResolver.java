package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.event.eventgroup.EventGroup;
import server.event.eventgroup.EventGroupType;

public class MinionAttackResolver extends Resolver {
    final Minion m1;
    final Minion m2;

    public MinionAttackResolver(Minion m1, Minion m2) {
        super(false);
        this.m1 = m1;
        this.m2 = m2;
    }

    @Override
    public void onResolve(ServerBoard b, List<Resolver> rl, List<Event> el) {
        EventGroup attackOrdered = new EventGroup(EventGroupType.MINIONATTACKORDER, List.of(this.m1, this.m2));
        b.pushEventGroup(attackOrdered);
        b.processEvent(rl, el, new EventMinionAttack(this.m1, this.m2));
        if (this.m1.alive && this.m1.isInPlay()) {
            b.pushEventGroup(new EventGroup(EventGroupType.ONATTACK, List.of(this.m1)));
            List<Resolver> list = this.m1.onAttack(this.m2);
            this.resolveList(b, list, el, list);
            b.popEventGroup();
        }
        if (this.m1.alive && this.m1.isInPlay() && !(this.m2 instanceof Leader)) {
            b.pushEventGroup(new EventGroup(EventGroupType.CLASH, List.of(this.m1)));
            List<Resolver> list = this.m1.clash(this.m2);
            this.resolveList(b, list, el, list);
            b.popEventGroup();
        }
        if (this.m2.alive && this.m2.isInPlay()) {
            b.pushEventGroup(new EventGroup(EventGroupType.ONATTACKED, List.of(this.m2)));
            List<Resolver> list = this.m2.onAttacked(this.m1);
            this.resolveList(b, list, el, list);
            b.popEventGroup();
        }
        if (this.m2.alive && this.m2.isInPlay() && !(this.m1 instanceof Leader)) {
            b.pushEventGroup(new EventGroup(EventGroupType.CLASH, List.of(this.m2)));
            List<Resolver> list = this.m2.clash(this.m1);
            this.resolveList(b, list, el, list);
            b.popEventGroup();
        }
        if (this.m1.alive && this.m1.isInPlay() && this.m2.alive && this.m2.isInPlay()) {
            List<Card> destroyed = new ArrayList<>(2);
            int damage1 = this.m1.finalStatEffects.getStat(EffectStats.ATTACK);
            int damage2 = this.m2.finalStatEffects.getStat(EffectStats.ATTACK);
            b.pushEventGroup(new EventGroup(EventGroupType.MINIONCOMBAT));
            DamageResolver d1 = new DamageResolver(this.m1, List.of(this.m2), List.of(damage1),
                    List.of(this.m1.finalStatEffects.getStat(EffectStats.POISONOUS) > 0), false, m1.getTooltip().attackAnimation);
            DamageResolver d2 = new DamageResolver(this.m2, List.of(this.m1), List.of(damage2),
                    List.of(this.m2.finalStatEffects.getStat(EffectStats.POISONOUS) > 0), false, m2.getTooltip().attackAnimation);
            this.resolve(b, rl, el, d1);
            this.resolve(b, rl, el, d2);
            destroyed.addAll(d1.destroyed);
            destroyed.addAll(d2.destroyed);
            b.popEventGroup(); // minion combat
            b.popEventGroup(); // attack order
            if (this.m1.finalStatEffects.getStat(EffectStats.BANE) > 0 && !(this.m2 instanceof Leader)
                    && !destroyed.contains(this.m2)) {
                destroyed.add(this.m2);
            }
            if (this.m2.finalStatEffects.getStat(EffectStats.BANE) > 0 && !(this.m1 instanceof Leader)
                    && !destroyed.contains(this.m1)) {
                destroyed.add(this.m1);
            }
            this.resolve(b, rl, el, new DestroyResolver(destroyed));
        } else {
            b.popEventGroup(); // attack order
        }
    }
}
