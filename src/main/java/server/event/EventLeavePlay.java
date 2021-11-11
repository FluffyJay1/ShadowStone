package server.event;

import java.util.*;

import server.*;
import server.card.*;

// deprecated for now
public class EventLeavePlay extends Event {
    public static final int ID = 24;
    public Card c;

    // this is just used as a hook for effects
    public EventLeavePlay(Card c) {
        super(ID);
        this.c = c;
    }

    @Override
    public String toString() {
        return this.id + " " + this.c.toReference() + "\n";
    }

    public static EventLeavePlay fromString(Board b, StringTokenizer st) {
        Card c = Card.fromReference(b, st);
        return new EventLeavePlay(c);
    }

    @Override
    public boolean conditions() {
        return this.c instanceof BoardObject;
    }
}
