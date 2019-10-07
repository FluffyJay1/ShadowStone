package server.event;

import java.lang.reflect.*;
import java.util.*;

import server.*;

public class Event {
	// always go full enterprise, if you start going half enterprise you're
	// fucking done for
	int id = 0; // id of 0 means no side effects
	public boolean send = true, rng = false; // rng = true means the AI has to
												// evaluate it differently
	public int priority = 0;

	public Event(int id, boolean rng) {
		this.id = id;
		this.rng = true;
	}

	public void resolve(List<Event> eventlist, boolean loopprotection) {

	}

	/*
	 * This undo method should only be used for internal purposes like AI and is
	 * never a valid action that a server sends to its clients
	 */
	public void undo() {

	}

	@Override
	public String toString() {
		return this.id + " " + this.rng + "\n";
	}

	public static Event createFromString(Board b, StringTokenizer st) {
		int id = Integer.parseInt(st.nextToken());
		if (id == 0) {
			boolean rng = Boolean.parseBoolean(st.nextToken());
			return new Event(0, rng);
		} else {
			Class c = EventIDLinker.getClass(id);
			Event e = null;
			try {
				e = (Event) c.getMethod("fromString", Board.class, StringTokenizer.class).invoke(null, b, st);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} // SEND HELP
			return e;
		}
	}

	public boolean conditions() {
		return true;
	}
	// do not use
	// public static String resolveAll(LinkedList<Event> eventlist, boolean
	// loopprotection) {
	// String eventstring = "";
	// while (!eventlist.isEmpty()) {
	// String str = eventlist.getFirst().resolve(eventlist, false);
	// if (!str.isEmpty()) {
	// eventstring += str;
	// }
	// eventlist.removeFirst();
	// }
	// return eventstring;
	// }
}
