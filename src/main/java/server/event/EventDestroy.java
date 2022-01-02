package server.event;

import java.util.*;

import server.*;
import server.card.*;

// Changes references, should not run concurrent with other events
public class EventDestroy extends Event {
    // Shouldn't process this event outright, as it ignores lastwords triggers
    public static final int ID = 4;
    public final List<Card> cards;
    private List<Boolean> alive;
    private List<CardStatus> prevStatus;
    private List<Integer> prevPos;
    private List<Integer> prevLastBoardPos;
    final List<BoardObject> cardsLeavingPlay = new ArrayList<>(); // required for listeners

    public EventDestroy(List<Card> c) {
        super(ID);
        this.cards = c;
    }

    public EventDestroy(Card c) {
        this(List.of(c));
    }

    @Override
    public void resolve() {
        this.alive = new ArrayList<>(this.cards.size());
        this.prevStatus = new ArrayList<>(this.cards.size());
        this.prevPos = new ArrayList<>(this.cards.size());
        this.prevLastBoardPos = new ArrayList<>(this.cards.size());
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            this.alive.add(c.alive);
            this.prevStatus.add(c.status);
            this.prevPos.add(c.cardpos);
            this.prevLastBoardPos.add(0);
            if (c instanceof BoardObject) {
                this.prevLastBoardPos.set(i, ((BoardObject) c).lastBoardPos);
            }
            if (!c.status.equals(CardStatus.GRAVEYARD)) {
                // TODO increase shadows by 1
                c.alive = false;
                switch (c.status) {
                case HAND:
                    c.board.getPlayer(c.team).hand.cards.remove(c);
                    c.board.getPlayer(c.team).hand.updatePositions();
                    break;
                case BOARD:
                    if (c instanceof BoardObject) {
                        BoardObject b = (BoardObject) c;
                        b.lastBoardPos = b.cardpos;
                        b.board.removeBoardObject(b.team, b.cardpos);
                        this.cardsLeavingPlay.add(b);
                    }
                    break;
                case DECK:
                    c.board.getPlayer(c.team).deck.cards.remove(c);
                    c.board.getPlayer(c.team).deck.updatePositions();
                    break;
                case LEADER:
                    if (c instanceof Leader) {
                        this.cardsLeavingPlay.add((Leader) c);
                    }
                    break;
                default:
                    break;
                }
                c.cardpos = c.board.getGraveyard(c.team).size(); // just in case
                c.status = CardStatus.GRAVEYARD;
                c.board.getGraveyard(c.team).add(c);
            }
        }
    }

    @Override
    public void undo() {
        for (int i = this.cards.size() - 1; i >= 0; i--) {
            Card c = this.cards.get(i);
            c.alive = this.alive.get(i);
            CardStatus status = this.prevStatus.get(i);
            int pos = this.prevPos.get(i);
            if (!status.equals(CardStatus.GRAVEYARD)) {
                c.board.getGraveyard(c.team).remove(c);
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
                default: // undestroy leader lmao
                    break;
                }
                if (c instanceof BoardObject) {
                    ((BoardObject) c).lastBoardPos = this.prevLastBoardPos.get(i);
                }
                c.cardpos = pos; // just in case
                c.status = status;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.cards.size()).append(" ");
        for (Card card : this.cards) {
            builder.append(card.toReference());
        }
        return builder.append("\n").toString();
    }

    public static EventDestroy fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        ArrayList<Card> c = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Card card = Card.fromReference(b, st);
            c.add(card);
        }
        return new EventDestroy(c);
    }

    @Override
    public boolean conditions() {
        return !this.cards.isEmpty();
    }
}
