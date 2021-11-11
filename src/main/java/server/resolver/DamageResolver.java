package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class DamageResolver extends Resolver {
    List<Minion> targets;
    List<Integer> damage;
    List<Boolean> poisonous;
    public List<Card> destroyed;
    Effect source;

    /**
     * If true, this resolver will handle destruction. If false, hopefully the
     * parent resolver has a plan to eventually destroy what was marked for death by
     * this resolver, else we'll end up in an invalid state
     */
    boolean resolveDestroy;

    public DamageResolver(Effect source, List<Minion> targets, List<Integer> damage, List<Boolean> poisonous,
            boolean resolveDestroy) {
        super(false);
        this.source = source;
        this.targets = targets;
        this.damage = damage;
        this.poisonous = poisonous;
        this.destroyed = new LinkedList<>();
        this.resolveDestroy = resolveDestroy;
    }

    public DamageResolver(Effect source, Minion target, int damage, boolean poisonous, boolean resolveDestroy) {
        this(source, List.of(target), List.of(damage), List.of(poisonous), resolveDestroy);
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        b.processEvent(rl, el, new EventDamage(this.source, this.targets, this.damage, this.poisonous, this.destroyed));
        for (int i = 0; i < this.targets.size(); i++) {
            Minion m = this.targets.get(i);
            int damage = this.damage.get(i);
            List<Resolver> ondamage = m.onDamaged(damage);
            if (!ondamage.isEmpty()) {
                this.resolveList(b, ondamage, el, ondamage);
            }
        }
        if (this.resolveDestroy) {
            this.resolve(b, rl, el, new DestroyResolver(this.destroyed));
        }
    }
}
