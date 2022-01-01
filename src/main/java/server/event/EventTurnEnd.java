package server.event;

import java.util.*;

import server.*;

public class EventTurnEnd extends Event {
    public static final int ID = 14;
    public final Player p;

    public EventTurnEnd(Player p) {
        super(ID);
        this.p = p;
    }

    @Override
    public String toString() {
        return this.id + " " + this.p.team + "\n";
    }

    public static EventTurnEnd fromString(Board b, StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        Player p = b.getPlayer(team);
        return new EventTurnEnd(p);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
