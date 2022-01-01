package server.event;

import java.util.*;

import server.*;
import server.card.*;

// deprecated, see event groups
public class EventClash extends Event {
    public static final int ID = 27;
    public final Minion owner;

    public EventClash(Minion owner) {
        super(ID);
        this.owner = owner;
    }

    @Override
    public String toString() {
        return this.id + " " + Card.referenceOrNull(this.owner) + "\n";
    }

    public static EventClash fromString(Board b, StringTokenizer st) {
        Minion owner = (Minion) Card.fromReference(b, st);
        return new EventClash(owner);
    }

    @Override
    public boolean conditions() {
        return this.owner.isInPlay();
    }

}
