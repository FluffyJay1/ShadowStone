package server.event;

import java.lang.reflect.*;
import java.util.*;

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
public class Event {
    // always go full enterprise, if you start going half enterprise you're
    // fucking done for
    int id = 0; // id of 0 means no side effects
    public boolean send = true;

    public Event(int id) {
        this.id = id;
    }

    public void resolve() {

    }

    /*
     * This undo method should only be used for internal purposes like AI and is
     * never a valid action that a server sends to its clients
     */
    public void undo() {

    }

    @Override
    public String toString() {
        return this.id + "\n";
    }

    public static Event createFromString(Board b, StringTokenizer st) {
        int id = Integer.parseInt(st.nextToken());
        if (id == 0) {
            return new Event(0);
        } else {
            Class c = EventIDLinker.getClass(id);
            Event e = null;
            try {
                e = (Event) c.getMethod("fromString", Board.class, StringTokenizer.class).invoke(null, b, st);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } // SEND HELP
            return e;
        }
    }

    public boolean conditions() {
        return true;
    }

    public List<BoardObject> cardsEnteringPlay() {
        if (this instanceof EventCreateCard) {
            return ((EventCreateCard) this).cardsEnteringPlay;
        } else if (this instanceof EventPutCard) {
            return ((EventPutCard) this).cardsEnteringPlay;
        }
        return null;
    }

    public List<BoardObject> cardsLeavingPlay() {
        if (this instanceof EventBanish) {
            return ((EventBanish) this).cardsLeavingPlay;
        } else if (this instanceof EventDestroy) {
            return ((EventDestroy) this).cardsLeavingPlay;
        } else if (this instanceof EventPutCard) {
            return ((EventPutCard) this).cardsLeavingPlay;
        }
        return null;
    }
}
