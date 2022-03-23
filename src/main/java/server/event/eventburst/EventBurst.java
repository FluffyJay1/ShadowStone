package server.event.eventburst;

import client.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A higher (non-nestable) grouping of events that belongs to a team. Ending a
 * turn is split into two bursts, one for the ending of the turn of one player,
 * and one for the start of the turn for another player.
 *
 * The abstraction of grouping events into "bursts" lets us add metadata to
 * groups of events upon parsing. The main purpose of this is to let the real
 * board update in real time according to your actions (bursts), but to make the
 * real board update only for the enemy action (burst) that is currently being
 * animated.
 *
 * However this abstraction was brought in long after the existing eventstring
 * infrastructure was well established, so this is kind of hacky.
 */
public class EventBurst {
    public int team;
    public String eventString;

    public EventBurst(int team, String eventString) {
        this.team = team;
        this.eventString = eventString;
    }

    @Override
    public String toString() {
        return this.team + " " + this.eventString + Game.EVENT_BURST_END + " ";
    }

    public static EventBurst fromString(StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        String eventString = st.nextToken(Game.EVENT_BURST_END).trim();
        st.nextToken(" \n"); // THANKS STRING TOKENiZER
        return new EventBurst(team, eventString);
    }

    public static List<EventBurst> parseEventBursts(String s) {
        StringTokenizer st = new StringTokenizer(s);
        List<EventBurst> ret = new ArrayList<>();
        while (st.hasMoreTokens()) {
            EventBurst eb = EventBurst.fromString(st);
            ret.add(eb);
        }
        return ret;
    }
}
