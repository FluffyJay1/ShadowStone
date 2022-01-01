package server.event;

import java.util.*;

import server.*;
import server.card.*;

// deprecated, see event groups
public class EventLastWords extends Event {
    public static final int ID = 20;
    public final Card owner;

    public EventLastWords(Card owner) {
        super(ID);
        this.owner = owner;
    }

    @Override
    public String toString() {
        return this.id + " " + this.owner.toReference() + "\n";
    }

    public static EventLastWords fromString(Board b, StringTokenizer st) {
        Card owner = Card.fromReference(b, st);
        return new EventLastWords(owner);
    }

}
