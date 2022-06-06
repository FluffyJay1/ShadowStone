package server.card.effect.common;

import java.util.*;
import java.util.stream.Collectors;

import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamage;
import server.*;
import server.ai.AI;
import server.card.*;
import server.card.effect.Effect;
import server.event.*;
import server.resolver.*;
import server.resolver.meta.ResolverWithDescription;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

public class EffectLastWordsAlliedBlast extends Effect {
    int damage = 0;
    Class<? extends EventAnimationDamage> animation;

    // required for reflection
    public EffectLastWordsAlliedBlast() { }

    // sourceString: what to put in the description as where it came from
    public EffectLastWordsAlliedBlast(String sourceString, int damage, Class<? extends EventAnimationDamage> animation) {
        super("<b>Last Words</b>: deal " + damage + " damage to a random allied minion (from " + sourceString + ").");
        this.damage = damage;
        this.animation = animation;
    }

    @Override
    public ResolverWithDescription lastWords() {
        EffectLastWordsAlliedBlast effect = this; // just being lazy
        return new ResolverWithDescription(this.description, new Resolver(true) {
            @Override
            public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
                List<Minion> minions = b.getMinions(effect.owner.team, false, true).collect(Collectors.toList());
                if (!minions.isEmpty()) {
                    Minion victim = SelectRandom.from(minions);
                    this.resolve(b, rq, el, new DamageResolver(effect, victim, effect.damage, true, animation));
                }
            }
        });
    }

    @Override
    public double getLastWordsValue(int refs) {
        return -AI.valueOfMinionDamage(this.damage) / 3.;
    }

    @Override
    public String extraStateString() {
        return this.damage + " " + EventAnimationDamage.nameOrNull(this.animation);
    }

    @Override
    public void loadExtraState(Board b, StringTokenizer st) {
        this.damage = Integer.parseInt(st.nextToken());
        this.animation = EventAnimationDamage.fromString(st.nextToken());
    }
}
