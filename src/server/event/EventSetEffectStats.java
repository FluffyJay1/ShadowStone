package server.event;

import java.util.*;

import server.*;
import server.card.effect.*;

public class EventSetEffectStats extends Event {
	public static final int ID = 30;
	Effect target;
	Effect newStats;
	private Effect oldStats;

	public EventSetEffectStats(Effect target, Effect newStats) {
		super(ID, false);
		this.target = target;
		this.newStats = newStats;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		this.oldStats = this.target.copyEffectStats();
		this.target.resetStats();
		this.target.applyEffectStats(this.newStats);
		this.target.owner.updateEffectStats(true);
	}

	@Override
	public void undo() {
		this.target.resetStats();
		this.target.applyEffectStats(this.oldStats);
		this.target.owner.updateEffectStats(true);
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
