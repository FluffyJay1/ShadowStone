package server.resolver;

import java.util.*;

import client.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class BlastResolver extends Resolver {
    Effect source;
    int damage;
    Board b;
    int enemyTeam;

    public BlastResolver(Effect source, int damage) {
        super(true);
        this.source = source;
        this.damage = damage;
        this.b = source.owner.board;
        this.enemyTeam = source.owner.team * -1;
    }

    @Override
    public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
        List<Minion> minions = this.b.getMinions(this.enemyTeam, false, true);
        Minion target = null;
        if (minions.isEmpty()) {
            target = this.b.getPlayer(this.enemyTeam).leader;
        } else {
            target = Game.selectRandom(minions);
        }
        this.resolve(b, rl, el, new EffectDamageResolver(this.source, List.of(target), List.of(this.damage), true));
    }
}
