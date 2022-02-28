package server;

import server.card.BoardObject;
import server.card.CardText;

import java.lang.reflect.InvocationTargetException;

public abstract class BoardObjectText extends CardText {
    @Override
    public abstract BoardObject constructInstance(Board b);

    public static BoardObjectText fromString(String s) {
        try {
            return Class.forName(s).asSubclass(BoardObjectText.class).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

}
