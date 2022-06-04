package server;

import client.tooltip.TooltipUnleashPower;
import server.card.CardText;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public abstract class UnleashPowerText extends CardText {
    @Override
    public final List<Effect> getEffects() {
        TooltipUnleashPower tooltip = this.getTooltip();
        Effect e = new Effect("", new EffectStats(tooltip.cost));
        e.effectStats.set.set(Stat.ATTACKS_PER_TURN, 1);
        List<Effect> special = this.getSpecialEffects();
        int specialSize = 0;
        if (special != null) {
            specialSize = special.size();
        }
        List<Effect> ret = new ArrayList<>(specialSize + 1);
        ret.add(e);
        if (special != null) {
            ret.addAll(special);
        }
        return ret;
    }

    @Override
    public final UnleashPower constructInstance(Board b) {
        return new UnleashPower(b, this);
    }

    public static UnleashPowerText fromString(String s) {
        try {
            return Class.forName(s).asSubclass(UnleashPowerText.class).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract List<Effect> getSpecialEffects();
    public abstract TooltipUnleashPower getTooltip();
}
