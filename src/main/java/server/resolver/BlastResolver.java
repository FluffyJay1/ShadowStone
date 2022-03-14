package server.resolver;

import java.util.*;
import java.util.stream.Collectors;

import client.*;
import client.ui.game.visualboardanimation.eventanimation.attack.EventAnimationDamage;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;
import server.resolver.util.ResolverQueue;

public class BlastResolver extends Resolver {
    final Effect source;
    final int damage;
    final Board b;
    final int enemyTeam;
    final Class<? extends EventAnimationDamage> animation;

    public BlastResolver(Effect source, int damage, Class<? extends EventAnimationDamage> animation) {
        super(true);
        this.source = source;
        this.damage = damage;
        this.b = source.owner.board;
        this.enemyTeam = source.owner.team * -1;
        this.animation = animation;
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        List<Minion> minions = this.b.getMinions(this.enemyTeam, false, true).collect(Collectors.toList());
        Minion target;
        if (minions.isEmpty()) {
            target = this.b.getPlayer(this.enemyTeam).getLeader().orElse(null);
        } else {
            target = Game.selectRandom(minions);
        }
        if (target != null) {
            this.resolve(b, rq, el, new DamageResolver(this.source, List.of(target), List.of(this.damage), true, this.animation));
        }
    }
}
