package server.playeraction;

import java.lang.reflect.*;
import java.util.*;

import server.*;
import server.resolver.*;

public abstract class PlayerAction {
    final int id; // literally just copying off of event

    public PlayerAction(int id) {
        this.id = id;
    }

    /**
     * Perform the given player action
     * 
     * @param b The board to perform on
     * @return The results of the action performed, may be empty if nothing happens
     *         but won't be null
     */
    public ResolutionResult perform(Board b) {
        return new ResolutionResult(new LinkedList<>(), false);
    }

    @Override
    public String toString() {
        return this.id + "\n";
    }

    public static PlayerAction createFromString(Board b, StringTokenizer st) {
        int id = Integer.parseInt(st.nextToken());
        Class<? extends  PlayerAction> c = ActionIDLinker.getClass(id);
        PlayerAction e = null;
        try {
            e = (PlayerAction) c.getMethod("fromString", Board.class, StringTokenizer.class).invoke(null, b, st);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return e;
    }
}
