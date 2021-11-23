package server.event;

import java.util.*;

import client.*;
import server.*;
import server.card.*;
import server.card.unleashpower.*;

// Changes references, should not run concurrent with other events
public class EventCreateCard extends Event {
    public static final int ID = 2;
    List<Card> cards;
    CardStatus status;
    int team;
    List<Integer> cardpos;
    private UnleashPower prevUP;
    private Leader prevLeader;
    private List<Boolean> successful;
    public List<Card> markedForDeath;
    List<BoardObject> cardsEnteringPlay = new ArrayList<>();

    public EventCreateCard(List<Card> cards, int team, CardStatus status, List<Integer> cardpos,
            List<Card> markedForDeath) {
        super(ID);
        this.cards = cards;
        this.team = team;
        this.status = status;
        this.cardpos = cardpos;
        this.markedForDeath = markedForDeath;
        this.successful = new ArrayList<>();
    }

    @Override
    public void resolve() {
        if (this.markedForDeath == null) {
            this.markedForDeath = new ArrayList<>();
        }
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            int cardpos = this.cardpos.get(i);
            c.team = this.team;
            c.status = this.status;
            Board b = c.board;
            switch (this.status) {
            case HAND:
                Hand relevantHand = b.getPlayer(this.team).hand;
                if (relevantHand.cards.size() < relevantHand.maxsize) {
                    int temppos = cardpos == -1 ? (int) relevantHand.cards.size() : cardpos;
                    temppos = Math.min(temppos, relevantHand.cards.size());
                    relevantHand.cards.add(temppos, c);
                    relevantHand.updatePositions();
                    this.successful.add(true);
                } else {
                    this.markedForDeath.add(c);
                    this.successful.add(false);
                }
                break;
            case BOARD:
                // TODO: CHECK IF BOARD IS FULL
                if (c instanceof BoardObject) {
                    BoardObject bo = (BoardObject) c;
                    this.cardsEnteringPlay.add(bo);
                    b.addBoardObject(bo, this.team, cardpos == -1 ? c.board.getBoardObjects(this.team).size() : cardpos);
                    bo.lastBoardPos = bo.cardpos;
                    if (c instanceof Minion) {
                        ((Minion) c).summoningSickness = true;
                    }
                    this.successful.add(true);
                }
                break;
            case DECK:
                Deck relevantDeck = b.getPlayer(this.team).deck;
                int temppos = cardpos == -1 ? (int) relevantDeck.cards.size() : cardpos;
                temppos = Math.min(temppos, relevantDeck.cards.size());
                relevantDeck.cards.add(temppos, c);
                relevantDeck.updatePositions();
                this.successful.add(true);
                break;
            case UNLEASHPOWER:
                if (this.prevUP == null) {
                    this.prevUP = b.getPlayer(this.team).unleashPower;
                }
                b.getPlayer(this.team).unleashPower = (UnleashPower) c;
                ((UnleashPower) c).p = b.getPlayer(this.team);
                this.successful.add(true);
                break;
            case LEADER:
                if (this.prevLeader == null) {
                    this.prevLeader = b.getPlayer(this.team).leader;
                }
                b.getPlayer(this.team).leader = (Leader) c;
                this.successful.add(true);
            default:
                this.successful.add(false);
                break;
            }
            if (b.isClient) {
                b.cardsCreated.add(c);
            }
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            Board b = c.board;
            CardStatus status = c.status;
            if (this.successful.get(i)) {
                switch (status) {
                case HAND:
                    Hand relevantHand = b.getPlayer(this.team).hand;
                    relevantHand.cards.remove(c);
                    relevantHand.updatePositions();
                    break;
                case BOARD:
                    if (c instanceof BoardObject) {
                        b.removeBoardObject((BoardObject) c);
                    }
                    break;
                case DECK:
                    Deck relevantDeck = b.getPlayer(this.team).deck;
                    relevantDeck.cards.remove(c);
                    relevantDeck.updatePositions();
                    break;
                case UNLEASHPOWER:
                    b.getPlayer(this.team).unleashPower = this.prevUP;
                    break;
                case LEADER:
                    b.getPlayer(this.team).leader = this.prevLeader;
                default:
                    break;
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
        List<Card> cards = new LinkedList<>();
        for (int i = 0; i < numCards; i++) {
            Card c = Card.createFromConstructorString(b, st);
            cards.add(c);
            if (b instanceof VisualBoard) {
                c.realCard = ((VisualBoard) b).realBoard.cardsCreated.remove(0);
                ((VisualBoard) b).uiBoard.addCard(c);
            }
        }
        int team = Integer.parseInt(st.nextToken());
        String sStatus = st.nextToken();
        CardStatus csStatus = null;
        for (CardStatus cs : CardStatus.values()) {
            if (cs.toString().equals(sStatus)) {
                csStatus = cs;
            }
        }
        List<Integer> cardpos = new LinkedList<>();
        for (int i = 0; i < numCards; i++) {
            cardpos.add(Integer.parseInt(st.nextToken()));
        }
        return new EventCreateCard(cards, team, csStatus, cardpos, null);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
