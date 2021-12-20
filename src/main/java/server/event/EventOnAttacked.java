package server.event;

import java.util.*;

import server.*;
import server.card.*;

// deprecated, see event groups
public class EventOnAttacked extends Event {
    public static final int ID = 26;
    public final Minion owner;

    public EventOnAttacked(Minion owner) {
        super(ID);
        this.owner = owner;
    }

    @Override
    public String toString() {
        return this.id + " " + this.owner.toReference() + "\n";
    }

    public static EventOnAttacked fromString(Board b, StringTokenizer st) {
        Minion owner = (Minion) Card.fromReference(b, st);
        return new EventOnAttacked(owner);
    }

    @Override
    public boolean conditions() {
        return this.owner.isInPlay();
    }

}
