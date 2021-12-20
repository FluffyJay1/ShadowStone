package server.event;

import java.util.*;

import server.*;
import server.card.*;

// deprecated, see event groups
public class EventOnAttack extends Event {
    public static final int ID = 25;
    public final Minion owner;

    public EventOnAttack(Minion owner) {
        super(ID);
        this.owner = owner;
    }

    @Override
    public String toString() {
        return this.id + " " + Card.referenceOrNull(this.owner) + "\n";
    }

    public static EventOnAttack fromString(Board b, StringTokenizer st) {
        Minion owner = (Minion) Card.fromReference(b, st);
        return new EventOnAttack(owner);
    }

    @Override
    public boolean conditions() {
        return this.owner.isInPlay();
    }

}
