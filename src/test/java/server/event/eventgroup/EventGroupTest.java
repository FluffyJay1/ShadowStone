package server.event.eventgroup;

import org.junit.jupiter.api.Test;

import server.Board;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.StringTokenizer;

public class EventGroupTest {
    @Test
    void EventGroupParseTest() {
        String groupString = "g NORMAL 0\n";
        EventGroup group = EventGroup.fromString(new Board(), new StringTokenizer(groupString));
        assertEquals(EventGroupType.NORMAL, group.type);
        assertEquals(0, group.cards.size());
    }
    
}
