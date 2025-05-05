package server.resolver;

import java.util.*;

import org.jetbrains.annotations.NotNull;

import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamage;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class DamageResolver extends Resolver {
    final List<? extends Minion> targets;
    final List<Integer> damage;
    public final List<Card> destroyed;
    public EventDamage event;
    Effect effectSource;
    final Card cardSource;
    final String animationString;

    /**
     * If true, this resolver will handle destruction. If false, hopefully the
     * parent resolver has a plan to eventually destroy what was marked for death by
     * this resolver, else we'll end up in an invalid state
     */
    final boolean resolveDestroy;

    public DamageResolver(Card source, List<? extends Minion> targets, List<Integer> damage,
            boolean resolveDestroy, @NotNull String animationString) {
        super(false);
        this.cardSource = source;
        this.targets = targets;
        this.damage = damage;
        this.destroyed = new LinkedList<>();
        this.resolveDestroy = resolveDestroy;
        this.animationString = animationString;
    }

    public DamageResolver(Card source, List<? extends Minion> targets, List<Integer> damage,
            boolean resolveDestroy, @NotNull EventAnimationDamage eventAnimationDamage) {
        this(source, targets, damage, resolveDestroy, EventAnimation.stringOrNull(eventAnimationDamage));
    }

    public DamageResolver(Effect source, List<? extends Minion> targets, List<Integer> damage,
                          boolean resolveDestroy, @NotNull String animationString) {
        this(source.owner, targets, damage, resolveDestroy, animationString);
        this.effectSource = source;
    }

    public DamageResolver(Effect source, List<? extends Minion> targets, List<Integer> damage,
                          boolean resolveDestroy, @NotNull EventAnimationDamage eventAnimationDamage) {
        this(source, targets, damage, resolveDestroy, EventAnimation.stringOrNull(eventAnimationDamage));
    }

    public DamageResolver(Effect source, List<? extends Minion> targets, int damage,
                          boolean resolveDestroy, @NotNull EventAnimationDamage eventAnimationDamage) {
        this(source.owner, targets, Collections.nCopies(targets.size(), damage), resolveDestroy, eventAnimationDamage);
        this.effectSource = source;
    }

    public DamageResolver(Effect source, Minion target, int damage,
                          boolean resolveDestroy, @NotNull String animationString) {
        this(source.owner, List.of(target), List.of(damage), resolveDestroy, animationString);
        this.effectSource = source;
    }

    public DamageResolver(Effect source, Minion target, int damage,
                          boolean resolveDestroy, @NotNull EventAnimationDamage eventAnimationDamage) {
        this(source, target, damage, resolveDestroy, EventAnimation.stringOrNull(eventAnimationDamage));
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
                processedDamage.add(this.damage.get(i) > 0 ? this.damage.get(i) : 0);
            }
        }
        if (this.effectSource != null) {
            this.event = b.processEvent(rq, el, new EventDamage(this.effectSource, processedTargets, processedDamage, this.destroyed, this.animationString));
        } else {
            this.event = b.processEvent(rq, el, new EventDamage(this.cardSource, processedTargets, processedDamage, this.destroyed, this.animationString));
        }

        if (this.cardSource.finalStats.get(Stat.LIFESTEAL) > 0) {
            int totalHeal = this.event.actualNonOverkillDamage.stream()
                    .reduce(0, Integer::sum, Integer::sum);
            if (totalHeal > 0) {
                this.cardSource.player.getLeader().ifPresent(l -> {
                    this.resolve(b, rq, el, new RestoreResolver(this.effectSource, l, totalHeal));
                });
            }
        }

        for (int i = 0; i < processedTargets.size(); i++) {
            Minion m = processedTargets.get(i);
            int damage = this.event.actualDamage.get(i);
            rq.addAll(m.onDamaged(damage));
        }
        if (this.resolveDestroy) {
            this.resolve(b, rq, el, new DestroyResolver(this.destroyed, EventDestroy.Cause.NATURAL));
        }
    }
}
