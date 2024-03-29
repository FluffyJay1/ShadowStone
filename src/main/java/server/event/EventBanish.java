package server.event;

import java.util.*;

import client.Game;
import client.PendingPlayPositioner;
import server.*;
import server.card.*;

// Changes references, should not run concurrent with other events
public class EventBanish extends Event {
    public static final int ID = 18;
    public final List<? extends Card> cards;
    private List<Boolean> alive;
    private List<CardStatus> prevStatus;
    private List<Integer> prevPos;
    private List<Integer> prevLastBoardPos;
    private List<Integer> prevLastBoardEpoch;
    private int prevEpoch1, prevEpoch2;
    final List<BoardObject> cardsLeavingPlay = new ArrayList<>(); // required for listeners

    public EventBanish(List<? extends Card> c) {
        super(ID);
        this.cards = c;
    }

    @Override
    public void resolve(Board b) {
        this.alive = new ArrayList<>(this.cards.size());
        this.prevStatus = new ArrayList<>(this.cards.size());
        this.prevPos = new ArrayList<>(this.cards.size());
        this.prevLastBoardPos = new ArrayList<>(this.cards.size());
        this.prevLastBoardEpoch = new ArrayList<>(this.cards.size());
        if (this.cards.size() > 0) {
            this.prevEpoch1 = this.cards.get(0).board.getPlayer(1).getPlayArea().getCurrentEpoch();
            this.prevEpoch2 = this.cards.get(0).board.getPlayer(-1).getPlayArea().getCurrentEpoch();
        }
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            Player p = b.getPlayer(c.team);
            this.alive.add(c.alive);
            this.prevStatus.add(c.status);
            this.prevPos.add(c.getIndex());
            this.prevLastBoardPos.add(0);
            this.prevLastBoardEpoch.add(0);
            if (c instanceof BoardObject) {
                BoardObject bo = (BoardObject) c;
                this.prevLastBoardPos.set(i, bo.lastBoardPos);
                this.prevLastBoardEpoch.set(i, bo.lastBoardEpoch);
            }
            if (!c.status.equals(CardStatus.GRAVEYARD)) {
                c.alive = false;
                switch (c.status) {
                    case HAND -> p.getHand().remove(c);
                    case BOARD -> {
                        if (c instanceof BoardObject) {
                            BoardObject bo = (BoardObject) c;
                            bo.lastBoardPos = bo.getIndex();
                            if (bo.team == b.getLocalteam() && b instanceof PendingPlayPositioner) {
                                ((PendingPlayPositioner) b).getPendingPlayPositionProcessor().processOp(bo.getIndex(), null, false);
                            }
                            p.getPlayArea().remove(bo);
                            bo.lastBoardEpoch = p.getPlayArea().getCurrentEpoch();
                            this.cardsLeavingPlay.add(bo);
                        }
                    }
                    case DECK -> p.getDeck().remove(c);
                }
                c.status = CardStatus.BANISHED;
                p.getBanished().add(c);
            }
        }
    }

    @Override
    public void undo(Board b) {
        for (int i = this.cards.size() - 1; i >= 0; i--) {
            Card c = this.cards.get(i);
            Player p = b.getPlayer(c.team);
            c.alive = this.alive.get(i);
            CardStatus status = this.prevStatus.get(i);
            int pos = this.prevPos.get(i);
            if (!status.equals(CardStatus.GRAVEYARD)) {
                p.getBanished().remove(c);
                switch (status) {
                    case HAND -> p.getHand().add(pos, c);
                    case BOARD -> {
                        if (c instanceof BoardObject) {
                            BoardObject bo = (BoardObject) c;
                            p.getPlayArea().add(pos, bo);
                        }
                    }
                    case DECK -> p.getDeck().add(pos, c);
                }
                if (c instanceof BoardObject) {
                    BoardObject bo = (BoardObject) c;
                    bo.lastBoardPos = this.prevLastBoardPos.get(i);
                    bo.lastBoardEpoch = this.prevLastBoardEpoch.get(i);
                }
                c.status = status;
            }
        }
        if (this.cards.size() > 0) {
            this.cards.get(0).board.getPlayer(1).getPlayArea().resetHistoryToEpoch(this.prevEpoch1);
            this.cards.get(0).board.getPlayer(-1).getPlayArea().resetHistoryToEpoch(this.prevEpoch2);
        }
    }

    @Override
    public List<BoardObject> cardsLeavingPlay() {
        return this.cardsLeavingPlay;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.cards.size()).append(" ");
        for (Card card : this.cards) {
            builder.append(card.toReference());
        }
        builder.append(Game.EVENT_END);
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
        return !this.cards.isEmpty();
    }
}
