package server.event.eventgroup;

import client.Game;
import org.junit.jupiter.api.Test;

import server.ServerBoard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.StringTokenizer;

public class EventGroupTest {
    @Test
    void EventGroupParseTest() {
        String groupString = "g NORMAL " + Game.STRING_END + " 0";
        EventGroup group = EventGroup.fromString(new ServerBoard(1), new StringTokenizer(groupString));
        assertEquals(EventGroupType.NORMAL, group.type);
        assertEquals(0, group.cards.size());
    }
    
}
