package server.resolver;

import java.util.*;

import server.event.*;

//bean
public class ResolutionResult {
    /*
     * the main purpose of keeping track of a list of events, not their string
     * representations is to let the AI keep track of the events that happened so it
     * can undo them
     */
    public final List<Event> events;
    public boolean rng;

    public ResolutionResult() {
        this(new LinkedList<>(), false);
    }

    public ResolutionResult(List<Event> events, boolean rng) {
        this.events = events;
        this.rng = rng;
    }

    public ResolutionResult concat(ResolutionResult other) {
        this.events.addAll(other.events);
        this.rng = this.rng || other.rng;
        return this;
    }
}
