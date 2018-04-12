package server.event;

import java.util.LinkedList;

public class Event {
	// always go full enterprise, if you start going half enterprise you're
	// fucking done for
	public Event() {

	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		return this.toString();
	}

	public String toString() {
		return "";
	}

	public boolean conditions() {
		return true;
	}

	public static String resolveAll(LinkedList<Event> eventlist, boolean loopprotection) {
		String eventstring = "";
		while (!eventlist.isEmpty()) {
			String str = eventlist.getFirst().resolve(eventlist, false);
			if (!str.isEmpty()) {
				eventstring += str;
			}
			eventlist.removeFirst();
		}
		return eventstring;
	}
}
