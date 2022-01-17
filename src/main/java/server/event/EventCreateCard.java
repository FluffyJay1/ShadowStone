package server.event;

import java.util.*;

import client.*;
import server.*;
import server.card.*;
import server.card.unleashpower.*;

// Changes references, should not run concurrent with other events
public class EventCreateCard extends Event {
    public static final int ID = 2;
    public final List<Card> cards;
    public final CardStatus status;
    public final int team;
    public final List<Integer> cardpos;
    private UnleashPower prevUP;
    private Leader prevLeader;
    public final List<Boolean> successful;
    final List<BoardObject> cardsEnteringPlay = new ArrayList<>();

    public EventCreateCard(List<Card> cards, int team, CardStatus status, List<Integer> cardpos) {
        super(ID);
        this.cards = cards;
        this.team = team;
        this.status = status;
        this.cardpos = cardpos;
        this.successful = new ArrayList<>();
    }

    @Override
    public void resolve() {
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            int cardpos = this.cardpos.get(i);
            c.team = this.team;
            c.status = this.status;
            Board b = c.board;
            Player p = b.getPlayer(this.team);
            switch (this.status) {
                case HAND -> {
                    if (p.getHand().size() < p.maxHandSize) {
                        p.getHand().add(cardpos, c);
                        this.successful.add(true);
                    } else {
                        c.alive = false;
                        // TODO: add a shadow
                        this.successful.add(false);
                    }
                }
                case BOARD -> {
                    if (c instanceof BoardObject && p.getPlayArea().size() < p.maxPlayAreaSize) {
                        BoardObject bo = (BoardObject) c;
                        this.cardsEnteringPlay.add(bo);
                        p.getPlayArea().add(cardpos, bo);
                        bo.lastBoardPos = bo.getIndex();
                        if (c instanceof Minion) {
                            ((Minion) c).summoningSickness = true;
                        }
                        this.successful.add(true);
                        if (bo.team == b.localteam && b instanceof PendingPlayPositioner) {
                            ((PendingPlayPositioner) b).getPendingPlayPositionProcessor().processOp(bo.getIndex(), null, true);
                        }
                    } else {
                        this.successful.add(false);
                    }
                }
                case DECK -> {
                    p.getDeck().add(cardpos, c);
                    this.successful.add(true);
                }
                case UNLEASHPOWER -> {
                    if (this.prevUP == null) {
                        this.prevUP = p.getUnleashPower();
                    }
                    b.getPlayer(this.team).setUnleashPower((UnleashPower) c);
                    this.successful.add(true);
                }
                case LEADER -> {
                    if (this.prevLeader == null) {
                        this.prevLeader = p.getLeader();
                    }
                    p.setLeader((Leader) c);
                    this.successful.add(true);
                }
                default -> this.successful.add(false);
            }
            if (b instanceof ClientBoard) {
                ((ClientBoard) b).cardsCreated.add(c);
            }
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            Board b = c.board;
            Player p = b.getPlayer(this.team);
            CardStatus status = c.status;
            if (this.successful.get(i)) {
                switch (status) {
                    case HAND -> p.getHand().remove(c);
                    case BOARD -> {
                        if (c instanceof BoardObject) {
                            p.getPlayArea().remove((BoardObject) c);
                        }
                    }
                    case DECK -> p.getDeck().remove(c);
                    case UNLEASHPOWER -> b.getPlayer(this.team).setUnleashPower(this.prevUP);
                    case LEADER -> b.getPlayer(this.team).setLeader(this.prevLeader);
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.cards.size()).append(" ");
        for (Card c : this.cards) {
            builder.append(c.toConstructorString());
        }
        builder.append(this.team).append(" ").append(this.status.toString());
        for (Integer i : this.cardpos) {
            builder.append(" ").append(i);
        }
        builder.append("\n");
        return builder.toString();
    }

    public static EventCreateCard fromString(Board b, StringTokenizer st) {
        int numCards = Integer.parseInt(st.nextToken());
        List<Card> cards = new ArrayList<>(numCards);
        for (int i = 0; i < numCards; i++) {
            Card c = Card.createFromConstructorString(b, st);
            cards.add(c);
            if (b instanceof VisualBoard) {
                assert c != null;
                // link the ClientBoard version of the card with the VisualBoard version
                c.realCard = ((VisualBoard) b).realBoard.cardsCreated.remove(0);
                c.realCard.visualCard = c;
                ((VisualBoard) b).uiBoard.addCard(c);
            }
        }
        int team = Integer.parseInt(st.nextToken());
        String sStatus = st.nextToken();
        CardStatus csStatus = CardStatus.valueOf(sStatus);
        for (Card c : cards) {
            c.status = csStatus; //shh
        }
        List<Integer> cardpos = new ArrayList<>(numCards);
        for (int i = 0; i < numCards; i++) {
            cardpos.add(Integer.parseInt(st.nextToken()));
        }
        return new EventCreateCard(cards, team, csStatus, cardpos);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
