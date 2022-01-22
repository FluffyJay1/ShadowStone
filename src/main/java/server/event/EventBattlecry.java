package server.event;

import java.util.*;

import client.Game;
import server.*;
import server.card.*;

// deprecated, see event groups
public class EventBattlecry extends Event {
    public static final int ID = 19;
    public final Card owner;

    public EventBattlecry(Card owner) {
        super(ID);
        this.owner = owner;
    }

    @Override
    public String toString() {
        return this.id + " " + this.owner.toReference() + Game.EVENT_END;
    }

    public static EventBattlecry fromString(Board b, StringTokenizer st) {
        Card owner = Card.fromReference(b, st);
        return new EventBattlecry(owner);
    }

}
