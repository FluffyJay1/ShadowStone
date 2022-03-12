package server.card.cardset;

import java.lang.reflect.InvocationTargetException;

public abstract class ExpansionSet {
    public abstract CardSet getCards();

    public static ExpansionSet fromString(String s) {
        try {
            return Class.forName(s).asSubclass(ExpansionSet.class).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
