package server.event;

import java.util.*;

import client.Game;
import server.*;
import server.card.*;
import server.card.effect.*;

public class EventSetEffectStats extends Event {
    public static final int ID = 30;
    public final List<? extends Effect> targets;
    public final List<EffectStats> newStats;
    public List<Card> markedForDeath;
    private List<EffectStats> oldStats;
    private List<Integer> oldHealth;
    private List<Boolean> oldAlive;

    public EventSetEffectStats(List<? extends Effect> targets, List<EffectStats> newStats, List<Card> markedForDeath) {
        super(ID);
        assert targets.size() == newStats.size();
        this.targets = targets;
        this.newStats = newStats;
        this.markedForDeath = Objects.requireNonNullElseGet(markedForDeath, ArrayList::new);
    }

    @Override
    public void resolve(Board b) {
        this.oldStats = new ArrayList<>(this.targets.size());
        this.oldHealth = new ArrayList<>(this.targets.size());
        this.oldAlive = new ArrayList<>(this.targets.size());
        for (int i = 0; i < this.targets.size(); i++) {
            Effect e = this.targets.get(i);
            this.oldStats.add(e.effectStats.clone());
            this.oldAlive.add(e.owner.alive);
            this.oldHealth.add(0);
            e.effectStats.copy(this.newStats.get(i));
            e.owner.updateEffectStats(true);
            if (e.owner instanceof Minion) {
                Minion m = (Minion) e.owner;
                this.oldHealth.set(i, m.health);
                if (m.finalStatEffects.getStat(EffectStats.HEALTH) < m.health) {
                    m.health = m.finalStatEffects.getStat(EffectStats.HEALTH);
                }
                if (m.health <= 0 && m.alive && !this.markedForDeath.contains(m)) {
                    m.alive = false;
                    this.markedForDeath.add(m);
                }
            }
        }
    }

    @Override
    public void undo(Board b) {
        for (int i = this.targets.size() - 1; i >= 0; i--) {
            Effect e = this.targets.get(i);
            e.effectStats.copy(this.oldStats.get(i));
            e.owner.updateEffectStats(true);
            if (e.owner instanceof Minion) {
                Minion m = ((Minion) e.owner);
                m.health = this.oldHealth.get(i);
            }
            e.owner.alive = this.oldAlive.get(i);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.id).append(" ").append(this.targets.size()).append(" ");
        for (int i = 0; i < this.targets.size(); i++) {
            sb.append(this.targets.get(i).toReference()).append(this.newStats.get(i).toString());
        }
        sb.append(Game.EVENT_END);
        return sb.toString();
    }

    public static EventSetEffectStats fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        List<Effect> targets = new ArrayList<>(size);
        List<EffectStats> newStats = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            targets.add(Effect.fromReference(b, st));
            newStats.add(EffectStats.fromString(st));
        }
        return new EventSetEffectStats(targets, newStats, null);
    }
}
