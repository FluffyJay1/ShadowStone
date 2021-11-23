package server.event;

import java.util.*;

import server.*;
import server.card.*;

// deprecated event?
public class EventMill extends Event {
    public static final int ID = 7;
    Card c;

    public EventMill(Card c) {
        super(ID);
        this.c = c;
    }

    @Override
    public String toString() {
        return this.id + " " + this.c.toReference() + "\n";
    }

    public static EventMill fromString(Board b, StringTokenizer st) {
        Card c = Card.fromReference(b, st);
        return new EventMill(c);
    }

    @Override
    public boolean conditions() {
        return this.c.status == CardStatus.DECK;
    }
}
