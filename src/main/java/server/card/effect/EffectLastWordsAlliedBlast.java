package server.card.effect;

import java.util.*;

import client.*;
import server.*;
import server.card.*;
import server.event.*;
import server.resolver.*;

public class EffectLastWordsAlliedBlast extends Effect {
    int damage = 0;

    public EffectLastWordsAlliedBlast(String description) {
        super(description);
    }

    public EffectLastWordsAlliedBlast(int damage) {
        super("<b> Last Words: </b> Deal " + damage + " damage to a random allied minion.");
        this.damage = damage;
    }

    @Override
    public Resolver lastWords() {
        Effect effect = this; // just being lazy
        return new Resolver(true) {
            @Override
            public void onResolve(Board b, List<Resolver> rl, List<Event> el) {
                List<Minion> minions = b.getMinions(effect.owner.team, false, true);
                if (!minions.isEmpty()) {
                    Minion victim = Game.selectRandom(minions);
                    EffectLastWordsAlliedBlast parent = (EffectLastWordsAlliedBlast) effect;
                    this.resolve(b, rl, el, new EffectDamageResolver(effect, victim, parent.damage, true));
                }
            }
        };
    }

    @Override
    public String extraStateString() {
        return this.damage + " ";
    }

    @Override
    public void loadExtraState(Board b, StringTokenizer st) {
        this.damage = Integer.parseInt(st.nextToken());
        this.description = "<b> Last Words: </b> Deal " + this.damage + " damage to a random allied minion.";
    }
}