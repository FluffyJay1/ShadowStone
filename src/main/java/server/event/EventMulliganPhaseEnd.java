package server.event;

import client.Game;
import server.Board;

import java.util.StringTokenizer;

public class EventMulliganPhaseEnd extends Event {
    public static final int ID = 10;

    public EventMulliganPhaseEnd() {
        super(ID);
    }

    @Override
    public void resolve(Board b) {
        b.mulligan = false;
    }

    @Override
    public void undo(Board b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return this.id + Game.EVENT_END;
    }

    public static EventMulliganPhaseEnd fromString(Board b, StringTokenizer st) {
        return new EventMulliganPhaseEnd();
    }
}
