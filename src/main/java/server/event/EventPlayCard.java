package server.event;

import java.util.*;

import server.*;
import server.card.*;

public class EventPlayCard extends Event {
    public static final int ID = 11;
    public Player p;
    public Card c;
    int position;

    public EventPlayCard(Player p, Card c, int position) {
        super(ID);
        this.p = p;
        this.c = c;
        this.position = position;
    }

    @Override
    public String toString() {
        return this.id + " " + p.team + " " + position + " " + this.c.toReference() + "\n";
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
