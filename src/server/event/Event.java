package server.event;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;

public class Event {
	// always go full enterprise, if you start going half enterprise you're
	// fucking done for
	int id = 0; // id of 0 means no side effects
	public boolean send = true, resolvefirst = false;

	public Event(int id) {
		this.id = id;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {

	}

	public String toString() {
		return this.id + "\n";
	}

	public static Event createFromString(Board b, StringTokenizer st) {
		int id = Integer.parseInt(st.nextToken());
		if (id == 0) {
			return new Event(0);
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
