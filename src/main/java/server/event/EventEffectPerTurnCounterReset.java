package server.event;

import java.util.*;

import client.Game;
import server.*;
import server.card.effect.Effect;

public class EventEffectPerTurnCounterReset extends Event {
    // reset effects' "at most x times per turn" counters
    public static final int ID = 26;
    final List<Effect> effects;
    List<Map<String, Integer>> oldCounts;

    public EventEffectPerTurnCounterReset(List<Effect> effects) {
        super(ID);
        this.effects = effects;
        this.oldCounts = new ArrayList<>(effects.size());
    }

    @Override
    public void resolve(Board b) {
        for (Effect e : this.effects) {
            this.oldCounts.add(new HashMap<>(e.perTurnCounters));
            e.perTurnCounters.clear();
            if (b instanceof ServerBoard) {
                ((ServerBoard) b).effectsWithPerTurnCounters.remove(e);
            }
        }
    }

    @Override
    public void undo(Board b) {
        for (int i = 0; i < this.effects.size(); i++) {
            Effect e = this.effects.get(i);
            e.perTurnCounters.putAll(this.oldCounts.get(i));
            if (b instanceof ServerBoard && !this.oldCounts.isEmpty()) {
                ((ServerBoard) b).effectsWithPerTurnCounters.add(e);
            }
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

    public static EventEffectPerTurnCounterReset fromString(Board b, StringTokenizer st) {
        int numEffects = Integer.parseInt(st.nextToken());
        List<Effect> effects = new ArrayList<>(numEffects);
        for (int i = 0; i < numEffects; i++) {
            effects.add(Effect.fromReference(b, st));
        }
        return new EventEffectPerTurnCounterReset(effects);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
