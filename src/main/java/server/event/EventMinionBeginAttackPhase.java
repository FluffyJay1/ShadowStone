package server.event;

import java.util.*;

import server.*;
import server.card.*;

// DEPRECATED LOL
public class EventMinionBeginAttackPhase extends Event {
    // damage phase of attack
    public static final int ID = 9;
    public Minion m1, m2;

    public EventMinionBeginAttackPhase(Minion m1, Minion m2) {
        super(ID);
        this.m1 = m1;
        this.m2 = m2;
    }

    @Override
    public String toString() {
        return this.id + " " + m1.toReference() + m2.toReference() + "\n";
    }

    public static EventMinionBeginAttackPhase fromString(Board b, StringTokenizer st) {
        Card m1 = Card.fromReference(b, st);
        Card m2 = Card.fromReference(b, st);
        return new EventMinionBeginAttackPhase((Minion) m1, (Minion) m2);
    }

    @Override
    public boolean conditions() {
        return m1.isInPlay() && m2.isInPlay();
    }
}
