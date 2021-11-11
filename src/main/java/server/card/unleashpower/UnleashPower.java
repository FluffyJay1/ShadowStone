package server.card.unleashpower;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

public class UnleashPower extends Card {

    public Player p;
    public int unleashesThisTurn = 0;

    public UnleashPower(Board b, TooltipUnleashPower tooltip) {
        super(b, tooltip);
        Effect e = new Effect("", tooltip.cost);
        e.set.setStat(EffectStats.ATTACKS_PER_TURN, 1);
        this.addEffect(true, e);
    }

    public List<Resolver> onUnleashPre(Minion m) {
        List<Resolver> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            Resolver temp = e.onUnleashPre(m);
            if (temp != null) {
                list.add(temp);
            }
        }
        return list;
    }

    public List<Resolver> onUnleashPost(Minion m) {
        List<Resolver> list = new LinkedList<>();
        for (Effect e : this.getFinalEffects(true)) {
            Resolver temp = e.onUnleashPost(m);
            if (temp != null) {
                list.add(temp);
            }
        }
        return list;
    }
}
