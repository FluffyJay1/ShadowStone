package server.card;

import client.tooltip.TooltipCard;
import server.Board;
import server.card.effect.Effect;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Defines a pairing between what is written in a card's text area, and the
 * implementation of that behavior. For example, if the card text specifies a
 * battlecry, then getEffects() will contain an effect that has a battlecry
 * hook.
 */
public abstract class CardText implements Serializable {
    public abstract List<Effect> getEffects();
    public abstract TooltipCard getTooltip();
    public abstract Card constructInstance(Board b);

    @Override
    public String toString() {
        return this.getClass().getName() + " ";
    }

    public static CardText fromString(String s) {
        try {
            return Class.forName(s).asSubclass(CardText.class).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        return this.getClass().isAssignableFrom(o.getClass()) && o.getClass().isAssignableFrom(this.getClass());
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }
}
