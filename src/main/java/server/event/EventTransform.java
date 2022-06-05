package server.event;

import client.ClientBoard;
import client.Game;
import client.PendingPlayPositioner;
import client.VisualBoard;
import server.Board;
import server.Player;
import server.card.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// Changes references, should not run concurrent with other events
public class EventTransform extends Event {
    public static final int ID = 5;
    public final List<Card> cards;
    public final List<Card> into;
    private List<Boolean> alive;
    private List<CardStatus> prevStatus;
    private List<Integer> prevPos;
    private List<Integer> prevLastBoardPos;
    private List<Integer> prevLastBoardEpoch;
    private int prevEpoch1, prevEpoch2;

    public EventTransform(List<Card> c, List<Card> into) {
        super(ID);
        assert c.size() == into.size();
        this.cards = c;
        this.into = into;
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
            Card replacement = this.into.get(i);
            replacement.status = c.status;
            replacement.team = c.team;
            if (!c.status.equals(CardStatus.GRAVEYARD)) {
                c.alive = false;
                switch (c.status) {
                    case HAND -> p.getHand().set(c.getIndex(), replacement);
                    case BOARD -> {
                        if (c instanceof BoardObject && replacement instanceof BoardObject) {
                            BoardObject bo = (BoardObject) c;
                            BoardObject replacementBO = (BoardObject) replacement;
                            if (replacementBO instanceof Minion) {
                                ((Minion) replacementBO).summoningSickness = true;
                            }
                            bo.lastBoardPos = bo.getIndex();
                            p.getPlayArea().set(bo.getIndex(), replacementBO);
                            bo.lastBoardEpoch = p.getPlayArea().getCurrentEpoch();
                            if (bo.team == b.localteam && b instanceof PendingPlayPositioner) {
                                ((PendingPlayPositioner) b).getPendingPlayPositionProcessor().invalidate();
                            }
                        }
                    }
                    case DECK -> p.getDeck().set(c.getIndex(), replacement);
                }
                c.status = CardStatus.BANISHED;
                p.getBanished().add(c);
            }
            if (b instanceof ClientBoard) {
                ((ClientBoard) b).cardsCreated.add(replacement);
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
                    case HAND -> p.getHand().set(pos, c);
                    case BOARD -> {
                        if (c instanceof BoardObject) {
                            BoardObject bo = (BoardObject) c;
                            p.getPlayArea().set(pos, bo);
                        }
                    }
                    case DECK -> p.getDeck().set(pos, c);
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.cards.size()).append(" ");
        for (Card card : this.cards) {
            builder.append(card.toReference());
        }
        for (Card transformInto : this.into) {
            builder.append(transformInto.getCardText().toString());
        }
        builder.append(Game.EVENT_END);
        return builder.toString();
    }

    public static EventTransform fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        List<Card> c = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Card card = Card.fromReference(b, st);
            c.add(card);
        }
        List<Card> into = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            CardText cardText = CardText.fromString(st.nextToken());
            assert cardText != null;
            Card transformInto = cardText.constructInstance(b);
            into.add(transformInto);
            if (b instanceof VisualBoard) {
                // link the ClientBoard version of the card with the VisualBoard version
                transformInto.realCard = ((VisualBoard) b).realBoard.cardsCreated.remove(0);
                transformInto.realCard.visualCard = transformInto;
                ((VisualBoard) b).uiBoard.addCard(transformInto);
            }
        }
        return new EventTransform(c, into);
    }

    @Override
    public boolean conditions() {
        return !this.cards.isEmpty();
    }
}
