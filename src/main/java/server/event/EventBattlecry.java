package server.event;

import java.util.*;

import server.*;
import server.card.*;

//basically for display purposes
public class EventBattlecry extends Event {
    public static final int ID = 19;
    public Card owner;

    public EventBattlecry(Card owner) {
        super(ID);
        this.owner = owner;
    }

    @Override
    public String toString() {
        return this.id + " " + this.owner.toReference() + "\n";
    }

    public static EventBattlecry fromString(Board b, StringTokenizer st) {
        Card owner = Card.fromReference(b, st);
        return new EventBattlecry(owner);
    }

}
