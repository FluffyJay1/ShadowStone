package server.event;

import client.Game;
import server.Board;
import server.Player;

import java.util.StringTokenizer;

// for effects that explicitly add to your shadow count, without destroying cards
public class EventGainShadow extends Event {
    public static final int ID = 23;

    Player p;
    int amount;
    private int prevShadows; // lol

    public EventGainShadow(Player p, int amount) {
        super(ID);
        this.p = p;
        this.amount = amount;
    }

    @Override
    public void resolve(Board b) {
        this.prevShadows = p.shadows;
        p.shadows += amount;
    }

    @Override
    public void undo(Board b) {
        p.shadows = this.prevShadows;
    }

    @Override
    public String toString() {
        return ID + " " + this.p.team + " " + this.amount + Game.EVENT_END;
    }

    public static EventGainShadow fromString(Board b, StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        int amount = Integer.parseInt(st.nextToken());
        Player p = b.getPlayer(team);
        return new EventGainShadow(p, amount);
    }
}
