package server.card.target;

import java.util.List;

// java generics suck
public abstract class TargetList<T> implements Cloneable {
    public List<T> targeted;

    @Override
    public abstract TargetList<T> clone();
}
