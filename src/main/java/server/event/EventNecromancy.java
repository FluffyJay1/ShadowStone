package server.event;

import client.Game;
import server.Board;
import server.Player;
import server.card.effect.Effect;

import java.util.StringTokenizer;

public class EventNecromancy extends Event {
    public static final int ID = 17;

    public Effect source;
    int shadows, prevShadows;

    public EventNecromancy(Effect source, int shadows) {
        super(ID);
        this.source = source;
        this.shadows = shadows;
    }

    @Override
    public void resolve(Board b) {
        Player p = b.getPlayer(this.source.owner.team);
        this.prevShadows = p.shadows;
        p.shadows -= shadows;
    }

    @Override
    public void undo(Board b) {
        Player p = b.getPlayer(this.source.owner.team);
        p.shadows = this.prevShadows;
    }

    @Override
    public String toString() {
        return this.id + " " + this.shadows + " " + this.source.toReference() + Game.EVENT_END;
    }

    public static EventNecromancy fromString(Board b, StringTokenizer st) {
        int shadows = Integer.parseInt(st.nextToken());
        Effect effect = Effect.fromReference(b, st);
        return new EventNecromancy(effect, shadows);
    }
}
