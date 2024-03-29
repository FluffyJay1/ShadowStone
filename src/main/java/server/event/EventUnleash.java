package server.event;

import java.util.*;

import client.Game;
import client.PendingUnleash;
import server.*;
import server.card.*;

public class EventUnleash extends Event {
    public static final int ID = 16;
    public final Card source;
    public final Minion m;
    private int prevUnleashes;

    public EventUnleash(Card source, Minion m) {
        super(ID);
        this.source = source;
        this.m = m;
    }

    @Override
    public void resolve(Board b) {
        if (this.source instanceof UnleashPower) { // quality
            this.prevUnleashes = ((UnleashPower) this.source).unleashesThisTurn;
            ((UnleashPower) this.source).unleashesThisTurn++;
            if (this.source.team == this.source.board.getLocalteam() && this.source.board instanceof PendingUnleash.PendingUnleasher) {
                ((PendingUnleash.PendingUnleasher) this.source.board).getPendingUnleashProcessor().process(new PendingUnleash(this.source, this.m));
            }
        }
    }

    @Override
    public void undo(Board b) {
        if (this.source instanceof UnleashPower) { // quality
            ((UnleashPower) this.source).unleashesThisTurn = this.prevUnleashes;
        }
    }

    @Override
    public String toString() {
        return this.id + " " + this.source.toReference() + m.toReference() + Game.EVENT_END;
    }

    public static EventUnleash fromString(Board b, StringTokenizer st) {
        Card source = Card.fromReference(b, st);
        Minion m = (Minion) Card.fromReference(b, st);
        return new EventUnleash(source, m);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
