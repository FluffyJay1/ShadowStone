package server.event;

import java.util.*;

import client.Game;
import client.PendingMinionAttack;
import server.*;
import server.card.*;

public class EventMinionAttack extends Event {
    // start attack
    public static final int ID = 8;
    public final Minion m1;
    public final Minion m2;
    public final boolean playerOrdered;
    int prevAttacksThisTurn;

    public EventMinionAttack(Minion m1, Minion m2, boolean playerOrdered) {
        super(ID);
        this.m1 = m1;
        this.m2 = m2;
        this.playerOrdered = playerOrdered;
    }

    @Override
    public void resolve(Board b) {
        this.prevAttacksThisTurn = this.m1.attacksThisTurn;
        if (this.playerOrdered) {
            this.m1.attacksThisTurn++;
        }
        if (this.m1.team == this.m1.board.getLocalteam() && this.m1.board instanceof PendingMinionAttack.PendingMinionAttacker && this.playerOrdered) {
            ((PendingMinionAttack.PendingMinionAttacker) this.m1.board).getPendingMinionAttackProcessor().process(new PendingMinionAttack(this.m1, this.m2));
        }
    }

    @Override
    public void undo(Board b) {
        this.m1.attacksThisTurn = this.prevAttacksThisTurn;
    }

    @Override
    public String toString() {
        return this.id + " " + m1.toReference() + m2.toReference() + this.playerOrdered + Game.EVENT_END;
    }

    public static EventMinionAttack fromString(Board b, StringTokenizer st) {
        Card m1 = Card.fromReference(b, st);
        Card m2 = Card.fromReference(b, st);
        Boolean playerOrdered = Boolean.parseBoolean(st.nextToken());
        return new EventMinionAttack((Minion) m1, (Minion) m2, playerOrdered);
    }

    @Override
    public boolean conditions() {
        return m1.isInPlay() && m2.isInPlay();
    }
}
