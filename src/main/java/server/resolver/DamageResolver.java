package server.resolver;

import java.util.*;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamage;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class DamageResolver extends Resolver {
    final List<Minion> targets;
    final List<Integer> damage;
    public final List<Card> destroyed;
    Effect effectSource;
    final Card cardSource;
    final Class<? extends EventAnimationDamage> animation;

    /**
     * If true, this resolver will handle destruction. If false, hopefully the
     * parent resolver has a plan to eventually destroy what was marked for death by
     * this resolver, else we'll end up in an invalid state
     */
    final boolean resolveDestroy;

    public DamageResolver(Card source, List<Minion> targets, List<Integer> damage,
            boolean resolveDestroy, Class<? extends EventAnimationDamage> animation) {
        super(false);
        this.cardSource = source;
        this.targets = targets;
        this.damage = damage;
        this.destroyed = new LinkedList<>();
        this.resolveDestroy = resolveDestroy;
        this.animation = animation;
    }

    public DamageResolver(Effect source, List<Minion> targets, List<Integer> damage,
                          boolean resolveDestroy, Class<? extends EventAnimationDamage> animation) {
        this(source.owner, targets, damage, resolveDestroy, animation);
        this.effectSource = source;
    }

    public DamageResolver(Effect source, List<Minion> targets, int damage,
                          boolean resolveDestroy, Class<? extends EventAnimationDamage> animation) {
        this(source.owner, targets, Collections.nCopies(targets.size(), damage), resolveDestroy, animation);
        this.effectSource = source;
    }

    public DamageResolver(Effect source, Minion target, int damage,
                          boolean resolveDestroy, Class<? extends EventAnimationDamage> animation) {
        this(source.owner, List.of(target), List.of(damage), resolveDestroy, animation);
        this.effectSource = source;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        // filter out the targets that aren't even on the board at time of resolution
        List<Minion> processedTargets = new ArrayList<>(this.targets.size());
        List<Integer> processedDamage = new ArrayList<>(this.damage.size());
        for (int i = 0; i < this.targets.size(); i++) {
            Minion m = this.targets.get(i);
            if (m.isInPlay()) {
                processedTargets.add(m);
                processedDamage.add(this.damage.get(i));
            }
        }
        List<Boolean> processedPoisonous = Collections.nCopies(processedTargets.size(), this.cardSource.finalStatEffects.getStat(EffectStats.POISONOUS) > 0);
        if (this.effectSource != null) {
            b.processEvent(rq, el, new EventDamage(this.effectSource, processedTargets, processedDamage, processedPoisonous, this.destroyed, this.animation));
        } else {
            b.processEvent(rq, el, new EventDamage(this.cardSource, processedTargets, processedDamage, processedPoisonous, this.destroyed, this.animation));
        }
        for (int i = 0; i < processedTargets.size(); i++) {
            Minion m = processedTargets.get(i);
            int damage = processedDamage.get(i);
            rq.addAll(m.onDamaged(damage));
        }
        if (this.resolveDestroy) {
            this.resolve(b, rq, el, new DestroyResolver(this.destroyed));
        }
    }
}
