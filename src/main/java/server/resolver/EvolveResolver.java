package server.resolver;

import server.ServerBoard;
import server.card.CardText;
import server.card.Minion;
import server.card.MinionText;
import server.card.cardset.CardSet;
import server.card.effect.Stat;
import server.event.Event;
import server.resolver.util.ResolverQueue;
import utils.SelectRandom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// hearthstone evolve/devolve mechanic
public class EvolveResolver extends Resolver {
    List<Minion> minions;
    List<Integer> costChanges;
    public EvolveResolver(List<Minion> minions, List<Integer> costChanges) {
        super(true);
        this.minions = minions;
        this.costChanges = costChanges;
    }

    public EvolveResolver(List<Minion> minions, int costChange) {
        this(minions, Collections.nCopies(minions.size(), costChange));
    }

    @Override
    public void onResolve(ServerBoard b, ResolverQueue rq, List<Event> el) {
        List<CardText> replacements = new ArrayList<>(this.minions.size());
        for (int i = 0; i < this.minions.size(); i++) {
            Minion minion = this.minions.get(i);
            replacements.add(minion.getCardText()); // failsafe: just transform into itself
            int costChange = this.costChanges.get(i);
            while (true) { // repeatedly try with different costs until match found
                int newCost = minion.finalBasicStats.get(Stat.COST) + costChange;
                CardText replacement = SelectRandom.from(CardSet.PLAYABLE_SET.get().stream().filter(ct -> ct instanceof MinionText && ct.getTooltip().cost == newCost).toList());
                if (replacement != null) {
                    replacements.set(i, replacement);
                    break;
                } else {
                    // try again but with a cost slightly closer to the original card cost
                    if (costChange > 0) {
                        costChange--;
                    } else if (costChange < 0) {
                        costChange++;
                    } else {
                        // give up
                        break;
                    }
                }
            }
        }
        this.resolve(b, rq, el, new TransformResolver(this.minions, replacements));
    }
}
