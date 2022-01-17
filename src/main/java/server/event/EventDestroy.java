package server.event;

import java.util.*;

import client.PendingPlayPositioner;
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
    public List<Boolean> successful;
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
        this.successful = new ArrayList<>(this.cards.size());
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            Board b = c.board;
            Player p = b.getPlayer(c.team);
            this.alive.add(c.alive);
            this.prevStatus.add(c.status);
            this.prevPos.add(c.getIndex());
            this.prevLastBoardPos.add(0);
            this.successful.add(false);
            if (c instanceof BoardObject) {
                this.prevLastBoardPos.set(i, ((BoardObject) c).lastBoardPos);
            }
            if (!c.status.equals(CardStatus.GRAVEYARD)) {
                this.successful.set(i, true);
                // TODO increase shadows by 1
                c.alive = false;
                switch (c.status) {
                    case HAND -> p.getHand().remove(c);
                    case BOARD -> {
                        if (c instanceof BoardObject) {
                            BoardObject bo = (BoardObject) c;
                            bo.lastBoardPos = bo.getIndex();
                            if (bo.team == b.localteam && b instanceof PendingPlayPositioner) {
                                ((PendingPlayPositioner) b).getPendingPlayPositionProcessor().processOp(bo.getIndex(), null, false);
                            }
                            p.getPlayArea().remove(bo);
                            this.cardsLeavingPlay.add(bo);
                        }
                    }
                    case DECK -> p.getDeck().remove(c);
                    case LEADER -> {
                        if (c instanceof Leader) {
                            p.setLeader(null);
                            this.cardsLeavingPlay.add((Leader) c);
                        }
                    }
                }
                c.status = CardStatus.GRAVEYARD;
                p.getGraveyard().add(c);
            }
        }
    }

    @Override
    public void undo() {
        for (int i = this.cards.size() - 1; i >= 0; i--) {
            Card c = this.cards.get(i);
            Board b = c.board;
            Player p = b.getPlayer(c.team);
            c.alive = this.alive.get(i);
            CardStatus status = this.prevStatus.get(i);
            int pos = this.prevPos.get(i);
            if (this.successful.get(i)) {
                p.getGraveyard().remove(c);
                switch (status) {
                    case HAND -> p.getHand().add(pos, c);
                    case BOARD -> {
                        if (c instanceof BoardObject) {
                            BoardObject bo = (BoardObject) c;
                            p.getPlayArea().add(pos, bo);
                        }
                    }
                    case DECK -> p.getDeck().add(pos, c);
                    case LEADER -> p.setLeader((Leader) c);
                }
                if (c instanceof BoardObject) {
                    ((BoardObject) c).lastBoardPos = this.prevLastBoardPos.get(i);
                }
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
