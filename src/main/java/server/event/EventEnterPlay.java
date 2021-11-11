package server.event;

import java.util.*;

import server.*;
import server.card.*;

// deprecated for now, could be useful for event hooks later
public class EventEnterPlay extends Event {
    public static final int ID = 23;
    public Card c;

    // this is just used as a hook for effects
    public EventEnterPlay(Card c) {
        super(ID);
        this.c = c;
    }

    @Override
    public String toString() {
        return this.id + " " + this.c.toReference() + "\n";
    }

    public static EventEnterPlay fromString(Board b, StringTokenizer st) {
        Card c = Card.fromReference(b, st);
        return new EventEnterPlay(c);
    }

    @Override
    public boolean conditions() {
        return this.c instanceof BoardObject;
    }
}
