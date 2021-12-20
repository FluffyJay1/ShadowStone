package server.event;

import java.util.*;

import client.Game;
import server.*;
import server.card.effect.Effect;

public class EventUpdateEffectState extends Event {
    // Indicate an update in an effect's extra state
    public static final int ID = 9;
    final Effect effect;
    private String oldState;
    private final String newState;
    private final boolean alreadyResolved; // if in server and we already computed new state, avoid reloading it during event resolution

    public EventUpdateEffectState(Effect effect, String oldState, String newState, boolean alreadyResolved) {
        super(ID);
        this.effect = effect;
        this.oldState = oldState;
        this.newState = newState;
        this.alreadyResolved = alreadyResolved;
    }

    @Override
    public void resolve() {
        if (this.oldState == null) {
            this.oldState = this.effect.extraStateString();
        }
        if (this.newState != null && !this.alreadyResolved) {
            this.effect.loadExtraState(this.effect.owner.board, new StringTokenizer(this.newState));
        }
    }

    @Override
    public void undo() {
        if (this.oldState != null) {
            this.effect.loadExtraState(this.effect.owner.board, new StringTokenizer(this.oldState));
        }
    }

    @Override
    public String toString() {
        return this.id + " " + this.effect.toReference() + this.newState + Game.STRING_END + "\n";
    }

    public static EventUpdateEffectState fromString(Board b, StringTokenizer st) {
        Effect e = Effect.fromReference(b, st);
        String newState = st.nextToken(Game.STRING_END).trim();
        st.nextToken(" \n");
        return new EventUpdateEffectState(e, null, newState, false);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
