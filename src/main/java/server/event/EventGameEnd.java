package server.event;

import java.util.*;

import client.Game;
import server.*;

public class EventGameEnd extends Event {

    public static final int ID = 28;
    public final int victory;
    final Board b;

    public EventGameEnd(Board b, int victory) {
        super(ID);
        this.b = b;
        this.victory = victory;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void resolve(Board b) {
        this.b.winner = victory;
        // System.exit(0); // YES
    }

    @Override
    public void undo(Board b) {
        this.b.winner = 0; // gameend me irl
    }

    @Override
    public String toString() {
        return this.id + " " + victory + Game.EVENT_END;
    }

    public static EventGameEnd fromString(Board b, StringTokenizer st) {
        int vict = Integer.parseInt(st.nextToken());
        return new EventGameEnd(b, vict);
    }
}
