package server.event;

import java.util.*;

import client.Game;
import server.*;
import server.card.*;
import server.card.effect.*;

public class EventSetEffectStats extends Event {
    public static final int ID = 30;
    public final Effect target;
    final EffectStats newStats;
    public boolean markedForDeath;
    private EffectStats oldStats;
    private boolean oldAlive;

    public EventSetEffectStats(Effect target, EffectStats newStats) {
        super(ID);
        this.target = target;
        this.newStats = newStats;
    }

    @Override
    public void resolve() {
        this.oldStats = this.target.effectStats.clone();
        this.oldAlive = this.target.owner.alive;
        this.target.effectStats.copy(this.newStats);
        this.target.owner.updateEffectStats(true);
        if (this.target.owner instanceof Minion && ((Minion) this.target.owner).health <= 0 && target.owner.alive) {
            target.owner.alive = false;
            markedForDeath = true;
        }
    }

    @Override
    public void undo() {
        this.target.effectStats.copy(this.oldStats);
        this.target.owner.updateEffectStats(true);
        this.target.owner.alive = this.oldAlive;
        this.markedForDeath = false;
    }

    @Override
    public String toString() {
        return this.id + " " + this.target.toReference() + this.newStats.toString() + Game.EVENT_END;
    }

    public static EventSetEffectStats fromString(Board b, StringTokenizer st) {
        Effect target = Effect.fromReference(b, st);
        EffectStats newstats = EffectStats.fromString(st);
        return new EventSetEffectStats(target, newstats);
    }
}
