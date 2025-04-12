package server.event;

import java.util.*;

import client.Game;
import server.*;
import server.card.effect.Effect;

public class EventDecrementEffectTurnEndCount extends Event {
    // Indicate an update in an effect's extra state
    public static final int ID = 24;
    final List<Effect> effects;
    final List<Integer> oldTurnEndCounts;

    public EventDecrementEffectTurnEndCount(List<Effect> effects) {
        super(ID);
        this.effects = effects;
        this.oldTurnEndCounts = new ArrayList<>(effects.size());
    }

    @Override
    public void resolve(Board b) {
        for (int i = 0; i < this.effects.size(); i++) {
            Effect e = this.effects.get(i);
            this.oldTurnEndCounts.add(e.untilTurnEndCount);
            if (e.untilTurnEndCount != null) {
                e.untilTurnEndCount--;
            }
        }
    }

    @Override
    public void undo(Board b) {
        for (int i = 0; i < this.effects.size(); i++) {
            this.effects.get(i).untilTurnEndCount = this.oldTurnEndCounts.get(i);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.effects.size()).append(" ");
        for (Effect e : this.effects) {
            builder.append(e.toReference());
        }
        builder.append(Game.EVENT_END);
        return builder.toString();
    }

    public static EventDecrementEffectTurnEndCount fromString(Board b, StringTokenizer st) {
        int numEffects = Integer.parseInt(st.nextToken());
        List<Effect> effects = new ArrayList<>(numEffects);
        for (int i = 0; i < numEffects; i++) {
            effects.add(Effect.fromReference(b, st));
        }
        return new EventDecrementEffectTurnEndCount(effects);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
