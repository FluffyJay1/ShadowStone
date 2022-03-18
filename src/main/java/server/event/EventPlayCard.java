package server.event;

import java.util.*;

import client.Game;
import client.PendingPlay;
import server.*;
import server.card.*;

public class EventPlayCard extends Event {
    public static final int ID = 11;
    public final Player p;
    public final Card c;
    final int position;
    private int prevCardsPlayedThisTurn;

    public EventPlayCard(Player p, Card c, int position) {
        super(ID);
        this.p = p;
        this.c = c;
        this.position = position;
    }

    @Override
    public void resolve(Board b) {
        this.prevCardsPlayedThisTurn = this.p.cardsPlayedThisTurn; //paranoia
        if (this.c.team == b.localteam && b instanceof PendingPlay.PendingPlayer) {
            ((PendingPlay.PendingPlayer) b).getPendingPlayProcessor().process(new PendingPlay(this.c));
        }
        this.p.cardsPlayedThisTurn++;
    }

    @Override
    public void undo(Board b) {
        this.p.cardsPlayedThisTurn = this.prevCardsPlayedThisTurn;
    }

    @Override
    public String toString() {
        return this.id + " " + p.team + " " + position + " " + this.c.toReference() + Game.EVENT_END;
    }

    public static EventPlayCard fromString(Board b, StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        Player p = b.getPlayer(team);
        int position = Integer.parseInt(st.nextToken());
        Card c = Card.fromReference(b, st);
        return new EventPlayCard(p, c, position);
    }

    @Override
    public boolean conditions() {
        return true; // p.mana >= c.finalStatEffects.getStat(EffectStats.COST) &&
                        // this.c.status.equals(CardStatus.HAND);
    }
}
