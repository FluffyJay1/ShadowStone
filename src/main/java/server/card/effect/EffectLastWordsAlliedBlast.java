package server.card.effect;

import java.util.*;
import java.util.stream.Collectors;

import client.*;
import server.*;
import server.ai.AI;
import server.card.*;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;

public class EffectLastWordsAlliedBlast extends Effect {
    int damage = 0;

    // required for reflection
    public EffectLastWordsAlliedBlast() { }

    // sourceString: what to put in the description as where it came from
    public EffectLastWordsAlliedBlast(String sourceString, int damage) {
        super("<b>Last Words</b>: deal " + damage + " damage to a random allied minion (from " + sourceString + ").");
        this.damage = damage;
    }

    @Override
    public ResolverWithDescription lastWords() {
        EffectLastWordsAlliedBlast effect = this; // just being lazy
        return new ResolverWithDescription(this.description, new Resolver(true) {
            @Override
            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                List<Minion> minions = b.getMinions(effect.owner.team, false, true).collect(Collectors.toList());
                if (!minions.isEmpty()) {
                    Minion victim = Game.selectRandom(minions);
                    this.resolve(b, rq, el, new DamageResolver(effect, victim, effect.damage, true, null));
                }
            }
        });
    }

    @Override
    public double getLastWordsValue(int refs) {
        return AI.VALUE_PER_DAMAGE * -this.damage / 3.;
    }

    @Override
    public String extraStateString() {
        return this.damage + " ";
    }

    @Override
    public void loadExtraState(Board b, StringTokenizer st) {
        this.damage = Integer.parseInt(st.nextToken());
    }
}
