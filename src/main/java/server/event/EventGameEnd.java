package server.event;

import java.util.*;

import server.*;

public class EventGameEnd extends Event {

    public static final int ID = 28;
    public int victory;
    Board b;

    public EventGameEnd(Board b, int victory) {
        super(ID);
        this.b = b;
        this.victory = victory;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void resolve() {
        this.b.winner = victory;
        // System.exit(0); // YES
    }

    @Override
    public void undo() {
        this.b.winner = 0; // gameend me irl
    }

    @Override
    public String toString() {
        return this.id + " " + victory + "\n";
    }

    public static EventGameEnd fromString(Board b, StringTokenizer st) {
        int vict = Integer.parseInt(st.nextToken());
        return new EventGameEnd(b, vict);
    }
}
