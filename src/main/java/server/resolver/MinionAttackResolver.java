package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class MinionAttackResolver extends Resolver {
    Minion m1, m2;

    public MinionAttackResolver(Minion m1, Minion m2) {
        super(false);
        this.m1 = m1;
        this.m2 = m2;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        b.processEvent(rl, el, new EventMinionAttack(this.m1, this.m2));
        if (this.m1.alive) {
            List<Resolver> list = this.m1.onAttack(this.m2);
            this.resolveList(b, list, el, list);
        }
        if (this.m1.alive && !(this.m2 instanceof Leader)) {
            List<Resolver> list = this.m1.clash(this.m2);
            this.resolveList(b, list, el, list);
        }
        if (this.m2.alive) {
            List<Resolver> list = this.m2.onAttacked(this.m1);
            this.resolveList(b, list, el, list);
        }
        if (this.m2.alive && !(this.m1 instanceof Leader)) {
            List<Resolver> list = this.m2.clash(this.m1);
            this.resolveList(b, list, el, list);
        }
        if (this.m1.alive && this.m2.alive) {
            // beginattackphase event is deprecated
            // b.processEvent(rl, el, new EventMinionBeginAttackPhase(this.m1, this.m2));
            List<Card> destroyed = new ArrayList<>(2);
            int damage1 = this.m1.finalStatEffects.getStat(EffectStats.ATTACK);
            int damage2 = this.m2.finalStatEffects.getStat(EffectStats.ATTACK);
            b.processEvent(rl, el, new EventDamage(this.m1, List.of(this.m2), List.of(damage1),
                    List.of(this.m1.finalStatEffects.getStat(EffectStats.POISONOUS) > 0), true, destroyed));
            b.processEvent(rl, el, new EventDamage(this.m2, List.of(this.m1), List.of(damage2),
                    List.of(this.m2.finalStatEffects.getStat(EffectStats.POISONOUS) > 0), true, destroyed));
            if (this.m1.finalStatEffects.getStat(EffectStats.BANE) > 0 && !(this.m2 instanceof Leader)
                    && !destroyed.contains(this.m2)) {
                destroyed.add(this.m2);
            }
            if (this.m2.finalStatEffects.getStat(EffectStats.BANE) > 0 && !(this.m1 instanceof Leader)
                    && !destroyed.contains(this.m1)) {
                destroyed.add(this.m1);
            }
            this.resolve(b, rl, el, new DestroyResolver(destroyed));
        }
    }
}
