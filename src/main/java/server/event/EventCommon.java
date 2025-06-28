package server.event;

import java.util.List;

import server.card.Card;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

public class EventCommon {
    public static void markForDeathIfRequired(Card c, List<Card> markedForDeathListToAddTo) {
        if (c instanceof Minion) {
            Minion m = ((Minion) c);
            if (m.health <= 0 && c.alive) {
                c.alive = false;
                markedForDeathListToAddTo.add(m);
            }
        }
        if (c.finalStats.contains(Stat.COUNTDOWN)
                && c.finalStats.get(Stat.COUNTDOWN) <= 0 && c.alive) {
            c.alive = false;
            markedForDeathListToAddTo.add(c);
        }
    }

    public static void adjustMinionHealthAfterAddingEffect(Minion m, Effect e) {
        EffectStats es = e.effectStats;
        if (es.set.contains(Stat.HEALTH)) {
            m.health = es.set.get(Stat.HEALTH);
        }
        if (es.change.contains(Stat.HEALTH) && es.change.get(Stat.HEALTH) > 0) {
            m.health += es.change.get(Stat.HEALTH);
        }
        enforceMinionMaxHealth(m);
    }

    public static void enforceMinionMaxHealth(Minion m) {
        if (m.finalStats.get(Stat.HEALTH) < m.health) {
            m.health = m.finalStats.get(Stat.HEALTH);
        }
    }
}
