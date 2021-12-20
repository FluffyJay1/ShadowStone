package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventSetEffectStats extends Event {
    public static final int ID = 30;
    public final Effect target;
    final Effect newStats;
    public boolean markedForDeath;
    private Effect oldStats;
    private boolean oldAlive;

    public EventSetEffectStats(Effect target, Effect newStats) {
        super(ID);
        this.target = target;
        this.newStats = newStats;
    }

    @Override
    public void resolve() {
        this.oldStats = this.target.copyEffectStats();
        this.oldAlive = this.target.owner.alive;
        this.target.resetStats();
        this.target.applyEffectStats(this.newStats);
        this.target.owner.updateEffectStats(true);
        if (this.target.owner instanceof Minion && ((Minion) this.target.owner).health <= 0 && target.owner.alive) {
            target.owner.alive = false;
            markedForDeath = true;
        }
    }

    @Override
    public void undo() {
        this.target.resetStats();
        this.target.applyEffectStats(this.oldStats);
        this.target.owner.updateEffectStats(true);
        this.target.owner.alive = this.oldAlive;
        this.markedForDeath = false;
    }

    @Override
    public String toString() {
        return this.id + " " + this.target.toReference() + this.newStats.toString() + "\n";
    }

    public static EventSetEffectStats fromString(Board b, StringTokenizer st) {
        Effect target = Effect.fromReference(b, st);
        Effect newstats = Effect.fromString(b, st);
        return new EventSetEffectStats(target, newstats);
    }
}
