package server.card;

import client.tooltip.TooltipMinion;
import server.Board;
import server.card.effect.Effect;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public abstract class LeaderText extends MinionText {
    @Override
    public final Leader constructInstance(Board b) {
        return new Leader(b, this);
    }

    public static LeaderText fromString(String s) {
        try {
            return Class.forName(s).asSubclass(LeaderText.class).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected abstract List<Effect> getSpecialEffects();
    public abstract TooltipMinion getTooltip();
}
