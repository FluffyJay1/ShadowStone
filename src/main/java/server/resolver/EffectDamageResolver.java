package server.resolver;

import java.util.*;

import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamage;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class EffectDamageResolver extends Resolver {
    final Effect source;
    final List<Minion> targets;
    final List<Integer> damage;
    public List<Card> destroyed;

    /**
     * If true, this resolver will handle destruction. If false, hopefully the
     * parent resolver has a plan to eventually destroy what was marked for death by
     * this resolver, else we'll end up in an invalid state
     */
    final boolean resolveDestroy;
    final Class <? extends EventAnimationDamage> animation;

    public EffectDamageResolver(Effect source, List<Minion> targets, List<Integer> damage, boolean resolveDestroy, Class<? extends EventAnimationDamage> animation) {
        super(false);
        this.source = source;
        this.targets = targets;
        this.damage = damage;
        this.resolveDestroy = resolveDestroy;
        this.animation = animation;
    }

    public EffectDamageResolver(Effect source, Minion target, int damage, boolean resolveDestroy, Class<? extends EventAnimationDamage> animation) {
        this(source, List.of(target), List.of(damage), resolveDestroy, animation);
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        List<Boolean> poisonous = new ArrayList<>(this.targets.size());
        boolean isPoisonous = this.source.owner.finalStatEffects.getStat(EffectStats.POISONOUS) > 0;
        for (int i = 0; i < this.targets.size(); i++) {
            poisonous.add(isPoisonous);
        }
        DamageResolver damage = this.resolve(b, rl, el,
                new DamageResolver(this.source, this.targets, this.damage, poisonous, this.resolveDestroy, animation));
        this.destroyed = damage.destroyed;
    }
}
