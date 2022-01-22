package server.event.eventgroup;

import org.junit.jupiter.api.Test;

import server.Board;
import server.ServerBoard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.StringTokenizer;

public class EventGroupTest {
    @Test
    void EventGroupParseTest() {
        String groupString = "g NORMAL 0";
        EventGroup group = EventGroup.fromString(new ServerBoard(1), new StringTokenizer(groupString));
        assertEquals(EventGroupType.NORMAL, group.type);
        assertEquals(0, group.cards.size());
    }
    
}
