package server.event;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.card.Card;
import server.card.Target;

public class EventResolveTarget extends Event {
	// completely serverside, doesn't need to be sent over to client
	public static final int ID = 17;

	Target t;

	public EventResolveTarget(Target t) {
		super(ID);
		this.t = t;
		this.send = false;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.t.resolveTargets();
	}

	public String toString() {
		return this.id + "\n";
	}

	public static EventResolveTarget fromString(Board b, StringTokenizer st) {
		return new EventResolveTarget(new Target((Card) null));
	}

	public boolean conditions() {
		return true;
	}
}
