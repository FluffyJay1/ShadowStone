package server.event.eventgroup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import client.Game;
import server.Board;
import server.card.Card;

/**
 * Not the best design choice. Basically sometimes we wanna group certain events
 * together to make animations easier. So this class offers a general interface
 * for grouping events, even allowing for hierarchy. The board will push/pop
 * "current" event group contexts. We assume that the server sends entire
 * groups; if the server only partially sends groups, the behavior will be undefined.
 * Basically the board is like a state machine reading through the events,
 * when it sees an eventgroup string it pushes a type and pops it later
 */
public class EventGroup {
    public static final char PUSH_TOKEN = 'g';
    public static final char POP_TOKEN = 'p';
    public final EventGroupType type;
    public final List<Card> cards; // if there are relevant cards involved
    public String description; // for the DESCRIPTION eventgroup, which is used for a tooltip popup

    public EventGroup(EventGroupType type) {
        this(type, new LinkedList<>());
    }

    public EventGroup(EventGroupType type, List<Card> cards) {
        this(type, cards, "");
    }

    public EventGroup(EventGroupType type, List<Card> cards, String description) {
        this.type = type;
        this.cards = cards;
        this.description = description;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(); 
        sb.append(PUSH_TOKEN).append(" ").append(this.type.toString()).append(" ")
                .append(this.description).append(Game.STRING_END).append(" ")
                .append(this.cards.size()).append(" ");
        for (Card c : this.cards) {
            sb.append(c.toReference());
        }
        return sb.append(Game.EVENT_END).toString();
    }

    public static EventGroup fromString(Board b, StringTokenizer st) {
        if (!st.nextToken().equals("" + PUSH_TOKEN)) {
            throw new RuntimeException("eventgroup parsing error, wrong first token, something may be wrong");
        }
        EventGroupType type = EventGroupType.valueOf(st.nextToken());
        String description = st.nextToken(Game.STRING_END).trim();
        st.nextToken(" \n"); // THANKS STRING TOKENIZER
        int ncards = Integer.parseInt(st.nextToken());
        List<Card> cards = new ArrayList<>(ncards);
        for (int i = 0; i < ncards; i++) {
            cards.add(Card.fromReference(b, st));
        }
        return new EventGroup(type, cards, description);
    }

    // determine whether an eventline is a group
    public static boolean isGroup(String line) {
        if (line.isEmpty()) {
            return false;
        }
        char c = line.charAt(0);
        return c == POP_TOKEN || c == PUSH_TOKEN;
    }

    public static boolean isPush(String line) {
        if (line.isEmpty()) {
            return false;
        }
        char c = line.charAt(0);
        return c == PUSH_TOKEN;
    }

    public static boolean isPop(String line) {
        if (line.isEmpty()) {
            return false;
        }
        char c = line.charAt(0);
        return c == POP_TOKEN;
    }
}
