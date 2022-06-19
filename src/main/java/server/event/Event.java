package server.event;

import java.util.*;

import client.Game;
import server.*;
import server.card.*;

/**
 * The Event class represents all modifications to the board state, and any
 * board state can be recreated using a history of events. An event can only
 * contain side-effects, it doesn't create any other events on its own. However,
 * it can store information about its resolution. Events are created using the
 * Resolver class, and are sent to the clients in order to update board states.
 * 
 * @author Michael
 *
 */
public abstract class Event {
    // always go full enterprise, if you start going half enterprise you're
    // fucking done for
    final int id;
    public final boolean send = true;

    public Event(int id) {
        this.id = id;
    }

    public abstract void resolve(Board b);

    /*
     * This undo method should only be used for internal purposes like AI and is
     * never a valid action that a server sends to its clients
     */
    public abstract void undo(Board b);

    @Override
    public String toString() {
        return this.id + Game.EVENT_END;
    }

    public boolean conditions() {
        return true;
    }

    // overridden
    public List<BoardObject> cardsEnteringPlay() {
        return List.of();
    }

    // overridden
    public List<BoardObject> cardsLeavingPlay() {
        return List.of();
    }
}
