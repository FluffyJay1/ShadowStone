package server.event;

import java.util.*;

import server.*;
import server.card.*;

// deprecated, see event groups
public class EventFlag extends Event {
    public static final int ID = 21;
    public final Card owner;

    public EventFlag(Card owner) {
        super(ID);
        this.owner = owner;
    }

    @Override
    public String toString() {
        return this.id + " " + this.owner.toReference() + " " + "\n";
    }

    public static EventFlag fromString(Board b, StringTokenizer st) {
        Card owner = Card.fromReference(b, st);
        return new EventFlag(owner);
    }

}
