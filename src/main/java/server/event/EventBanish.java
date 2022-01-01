package server.event;

import java.util.*;

import server.*;
import server.card.*;

// Changes references, should not run concurrent with other events
public class EventBanish extends Event {
    public static final int ID = 18;
    public final List<Card> c;
    private List<Boolean> alive;
    private List<CardStatus> prevStatus;
    private List<Integer> prevPos;
    final List<BoardObject> cardsLeavingPlay = new ArrayList<>(); // required for listeners

    public EventBanish(List<Card> c) {
        super(ID);
        this.c = c;
    }

    @Override
    public void resolve() {
        this.alive = new ArrayList<>();
        this.prevStatus = new ArrayList<>();
        this.prevPos = new ArrayList<>();
        for (Card c : this.c) {
            this.alive.add(c.alive);
            this.prevStatus.add(c.status);
            this.prevPos.add(c.cardpos);
            if (!c.status.equals(CardStatus.GRAVEYARD)) {
                c.alive = false;
                switch (c.status) {
                case HAND:
                    c.board.getPlayer(c.team).hand.cards.remove(c);
                    c.board.getPlayer(c.team).hand.updatePositions();
                    break;
                case BOARD:
                    if (c instanceof BoardObject) {
                        BoardObject b = (BoardObject) c;
                        b.board.removeBoardObject(b.team, b.cardpos);
                        this.cardsLeavingPlay.add(b);
                    }
                    break;
                case DECK:
                    c.board.getPlayer(c.team).deck.cards.remove(c);
                    c.board.getPlayer(c.team).deck.updatePositions();
                    break;
                default: // banish leader lmao
                    break;
                }
                c.cardpos = c.board.banished.size(); // just in case
                c.board.banished.add(c);
            }
        }
    }

    @Override
    public void undo() {
        for (int i = this.c.size() - 1; i >= 0; i--) {
            Card c = this.c.get(i);
            c.alive = this.alive.get(i);
            CardStatus status = this.prevStatus.get(i);
            int pos = this.prevPos.get(i);
            if (!status.equals(CardStatus.GRAVEYARD)) {
                c.board.banished.remove(c);
                switch (status) {
                case HAND:
                    c.board.getPlayer(c.team).hand.cards.add(pos, c);
                    c.board.getPlayer(c.team).hand.updatePositions();
                    break;
                case BOARD:
                    if (c instanceof BoardObject) {
                        BoardObject b = (BoardObject) c;
                        b.board.addBoardObject(b, b.team, pos);
                    }
                    break;
                case DECK:
                    c.board.getPlayer(c.team).deck.cards.add(pos, c);
                    c.board.getPlayer(c.team).deck.updatePositions();
                    break;
                default: // unbanish leader lmao
                    break;
                }
                c.cardpos = pos; // just in case
                c.status = status;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.c.size()).append(" ");
        for (Card card : this.c) {
            builder.append(card.toReference());
        }
        builder.append("\n");
        return builder.toString();
    }

    public static EventBanish fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        ArrayList<Card> c = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Card card = Card.fromReference(b, st);
            c.add(card);
        }
        return new EventBanish(c);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
