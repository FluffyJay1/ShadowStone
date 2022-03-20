package server.card.target;

import server.Board;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.StringTokenizer;

// java generics suck
public abstract class TargetList<T> implements Cloneable {
    public List<T> targeted;

    @Override
    public abstract TargetList<T> clone();

    @Override
    public abstract String toString();

    public static TargetList<?> createFromString(Board b, StringTokenizer st) {
        String className = st.nextToken();
        try {
            return (TargetList<?>) Class.forName(className).asSubclass(TargetList.class).getMethod("fromString", Board.class, StringTokenizer.class).invoke(null, b, st);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
