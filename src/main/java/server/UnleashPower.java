package server;

import java.util.*;

import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.resolver.*;

public class UnleashPower extends Card {
    public int unleashesThisTurn = 0;

    public UnleashPower(Board b, UnleashPowerText unleashPowerText) {
        super(b, unleashPowerText);
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

    @Override
    public double getValue(int refs) {
        return 0;
    }

    @Override
    public void appendStringToBuilder(StringBuilder builder) {
        super.appendStringToBuilder(builder);
        builder.append(this.unleashesThisTurn).append(" ");
    }
}
