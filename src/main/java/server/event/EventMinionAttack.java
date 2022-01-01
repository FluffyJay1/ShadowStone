package server.event;

import java.util.*;

import server.*;
import server.card.*;

public class EventMinionAttack extends Event {
    // start attack
    public static final int ID = 8;
    public final Minion m1;
    public final Minion m2;
    int prevAttacksThisTurn;

    public EventMinionAttack(Minion m1, Minion m2) {
        super(ID);
        this.m1 = m1;
        this.m2 = m2;
    }

    @Override
    public void resolve() {
        this.prevAttacksThisTurn = this.m1.attacksThisTurn;
        this.m1.attacksThisTurn++;
    }

    @Override
    public void undo() {
        this.m1.attacksThisTurn = this.prevAttacksThisTurn;
    }

    @Override
    public String toString() {
        return this.id + " " + m1.toReference() + m2.toReference() + "\n";
    }

    public static EventMinionAttack fromString(Board b, StringTokenizer st) {
        Card m1 = Card.fromReference(b, st);
        Card m2 = Card.fromReference(b, st);
        return new EventMinionAttack((Minion) m1, (Minion) m2);
    }

    @Override
    public boolean conditions() {
        return m1.isInPlay() && m2.isInPlay();
    }
}
