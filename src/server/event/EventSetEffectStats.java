package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.effect.*;

public class EventSetEffectStats extends Event {
	public static final int ID = 30;
	Effect target;
	EffectStatChange newstats;

	public EventSetEffectStats(Effect target, EffectStatChange newstats) {
		super(ID);
		this.target = target;
		this.newstats = newstats;
	}

	@Override
	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.target.resetStats();
		this.target.applyEffectStats(newstats);
		this.target.owner.updateBasicEffectStats();
	}

	@Override
	public String toString() {
		return this.id + " " + this.target.toReference() + this.newstats.toString() + "\n";
	}

	public static EventSetEffectStats fromString(Board b, StringTokenizer st) {
		Effect target = Effect.fromReference(b, st);
		EffectStatChange newstats = (EffectStatChange) Effect.fromString(b, st);
		return new EventSetEffectStats(target, newstats);
	}
}
