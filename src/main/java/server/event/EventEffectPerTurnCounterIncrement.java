package server.event;

import java.util.*;

import client.Game;
import server.*;
import server.card.effect.Effect;

public class EventEffectPerTurnCounterIncrement extends Event {
    // Indicate an update in an effect's per-turn counter things
    public static final int ID = 25;
    final Effect effect;
    final String key;
    Integer oldCount;

    public EventEffectPerTurnCounterIncrement(Effect effect, String key) {
        super(ID);
        this.effect = effect;
        this.key = key;
    }

    @Override
    public void resolve(Board b) {
        this.oldCount = this.effect.perTurnCounters.put(this.key, this.effect.perTurnCounters.getOrDefault(this.key, 0) + 1);
        if (b instanceof ServerBoard) {
            ((ServerBoard) b).effectsWithPerTurnCounters.add(this.effect);
        }
    }

    @Override
    public void undo(Board b) {
        if (this.oldCount == null) {
            this.effect.perTurnCounters.remove(this.key);
        } else {
            this.effect.perTurnCounters.put(this.key, this.oldCount);
        }
        if (b instanceof ServerBoard) {
            ((ServerBoard) b).effectsWithPerTurnCounters.remove(this.effect);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.effect.toReference()).append(this.key).append(Game.STRING_END);
        builder.append(Game.EVENT_END);
        return builder.toString();
    }

    public static EventEffectPerTurnCounterIncrement fromString(Board b, StringTokenizer st) {
        Effect e = Effect.fromReference(b, st);
        String key = st.nextToken(Game.STRING_END).trim();
        st.nextToken(" \n"); // THANKS STRING TOKENIZER
        return new EventEffectPerTurnCounterIncrement(e, key);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
