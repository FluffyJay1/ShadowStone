package server.event;

import java.util.*;

import client.PendingUnleash;
import server.*;
import server.card.*;
import server.card.unleashpower.*;

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
    public void resolve() {
        if (this.source instanceof UnleashPower) { // quality
            this.prevUnleashes = ((UnleashPower) this.source).unleashesThisTurn;
            ((UnleashPower) this.source).unleashesThisTurn++;
            if (this.source.team == this.source.board.localteam && this.source.board instanceof PendingUnleash.PendingUnleasher) {
                ((PendingUnleash.PendingUnleasher) this.source.board).getPendingUnleashProcessor().process(new PendingUnleash(this.source, this.m));
            }
        }
    }

    @Override
    public void undo() {
        if (this.source instanceof UnleashPower) { // quality
            ((UnleashPower) this.source).unleashesThisTurn = this.prevUnleashes;
        }
    }

    @Override
    public String toString() {
        return this.id + " " + this.source.toReference() + m.toReference() + Target.listToString(m.getUnleashTargets())
                + "\n";
    }

    public static EventUnleash fromString(Board b, StringTokenizer st) {
        Card source = Card.fromReference(b, st);
        Minion m = (Minion) Card.fromReference(b, st);
        assert m != null;
        Target.setListFromString(m.getUnleashTargets(), b, st);
        return new EventUnleash(source, m);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
