package server.resolver;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class EffectDamageResolver extends Resolver {
    Effect source;
    List<Minion> targets;
    List<Integer> damage;
    public List<Card> destroyed;

    /**
     * If true, this resolver will handle destruction. If false, hopefully the
     * parent resolver has a plan to eventually destroy what was marked for death by
     * this resolver, else we'll end up in an invalid state
     */
    boolean resolveDestroy;

    public EffectDamageResolver(Effect source, List<Minion> targets, List<Integer> damage, boolean resolveDestroy) {
        super(false);
        this.source = source;
        this.targets = targets;
        this.damage = damage;
        this.resolveDestroy = resolveDestroy;
    }

    public EffectDamageResolver(Effect source, Minion target, int damage, boolean resolveDestroy) {
        this(source, List.of(target), List.of(damage), resolveDestroy);
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        List<Boolean> poisonous = new ArrayList<Boolean>(this.targets.size());
        boolean isPoisonous = this.source.owner.finalStatEffects.getStat(EffectStats.POISONOUS) > 0;
        for (int i = 0; i < this.targets.size(); i++) {
            poisonous.add(isPoisonous);
        }
        DamageResolver damage = this.resolve(b, rl, el,
                new DamageResolver(this.source, this.targets, this.damage, poisonous, this.resolveDestroy));
        this.destroyed = damage.destroyed;
    }
}
