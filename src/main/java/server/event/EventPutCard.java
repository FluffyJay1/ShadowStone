package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventPutCard extends Event {
    // for effects that put specific cards in hand or just draw cards
    public static final int ID = 12;
    public List<Card> c;
    public List<Integer> pos; // pos == -1 means last
    public CardStatus status;
    int targetTeam;
    private List<CardStatus> prevStatus;
    private List<List<Effect>> prevEffects;
    private List<List<Boolean>> prevMute;
    private List<Integer> prevPos;
    private List<Integer> prevLastBoardPos;
    private List<Integer> prevTeam;
    private List<Integer> prevHealth;
    private List<Integer> prevAttacks;
    private List<Boolean> prevSick;
    List<BoardObject> cardsEnteringPlay = new ArrayList<>();
    List<BoardObject> cardsLeavingPlay = new ArrayList<>();
    List<Card> markedForDeath;

    public EventPutCard(List<Card> c, CardStatus status, int team, List<Integer> pos, List<Card> markedForDeath) {
        super(ID);
        this.c = c;
        this.status = status;
        this.targetTeam = team;
        this.pos = pos;
        this.markedForDeath = markedForDeath;
    }

    public EventPutCard(Card c, CardStatus status, int team, int pos, List<Card> markedForDeath) {
        this(List.of(c), status, team, List.of(pos), markedForDeath);
    }

    @Override
    public void resolve() {
        if (this.markedForDeath == null) {
            this.markedForDeath = new ArrayList<>();
        }
        this.prevStatus = new LinkedList<>();
        this.prevEffects = new LinkedList<>();
        this.prevMute = new LinkedList<>();
        this.prevPos = new LinkedList<>();
        this.prevLastBoardPos = new LinkedList<>();
        this.prevTeam = new LinkedList<>();
        this.prevHealth = new LinkedList<>();
        this.prevAttacks = new LinkedList<>();
        this.prevSick = new LinkedList<>();
        for (int i = 0; i < this.c.size(); i++) {
            Card card = this.c.get(i);
            this.prevStatus.add(card.status);
            this.prevEffects.add(new LinkedList<>());
            this.prevMute.add(new LinkedList<>());
            this.prevPos.add(card.cardpos);
            this.prevLastBoardPos.add(0);
            this.prevTeam.add(card.team);
            this.prevHealth.add(0);
            this.prevAttacks.add(0);
            this.prevSick.add(true);
            if (card instanceof BoardObject) {
                this.prevLastBoardPos.set(i, ((BoardObject) card).lastBoardPos);
            }
            if (card instanceof Minion) {
                this.prevHealth.set(i, ((Minion) card).health);
                this.prevAttacks.set(i, ((Minion) card).attacksThisTurn);
                this.prevSick.set(i, ((Minion) card).summoningSickness);
            }
            Player p = card.board.getPlayer(card.team);
            switch (card.status) { // removing from
            case HAND:
                p.hand.cards.remove(card);
                p.hand.updatePositions();
                break;
            case BOARD:
                card.board.removeBoardObject((BoardObject) card);
                if (!card.status.equals(this.status)) {
                    this.cardsLeavingPlay.add((BoardObject) card);
                }
                break;
            case DECK:
                p.deck.cards.remove(card);
                p.deck.updatePositions();
                break;
            case LEADER:
                // wait
                break;
            default:
                break;
            }
            // goes against flow
            for (Effect be : card.getEffects(true)) {
                this.prevMute.get(i).add(be.mute);
            }
            if (card.status.ordinal() < this.status.ordinal()) {
                this.prevEffects.set(i, card.removeAdditionalEffects());
                if (card instanceof Minion) {
                    ((Minion) card).health = card.finalStatEffects.getStat(EffectStats.HEALTH);
                    ((Minion) card).attacksThisTurn = 0;
                }
            }
            card.team = this.targetTeam;
            if (this.status.equals(CardStatus.BOARD)) { // now adding to
                if (card instanceof BoardObject) {
                    // TODO: CHECK FOR ROOM ON BOARD
                    BoardObject bo = (BoardObject) card;
                    if (!card.status.equals(this.status)) {
                        this.cardsEnteringPlay.add(bo);
                    }
                    card.board.addBoardObject(bo, this.targetTeam, this.pos.get(i) == -1 ? card.board.getBoardObjects(card.team).size() : this.pos.get(i));
                    bo.lastBoardPos = bo.cardpos;
                    if (card instanceof Minion) {
                        ((Minion) card).summoningSickness = true;
                    }
                }
            } else {
                if (this.status.equals(CardStatus.HAND) && p.hand.cards.size() >= p.hand.maxsize) {
                    this.markedForDeath.add(card); // mill
                } else {
                    List<Card> cards = card.board.getCollection(this.targetTeam, this.status); // YEA
                    int temppos = this.pos.get(i) == -1 ? (int) cards.size() : this.pos.get(i);
                    temppos = Math.min(temppos, cards.size());
                    card.cardpos = temppos;
                    cards.add(temppos, card);
                    if (this.status.equals(CardStatus.HAND)) {
                        card.board.getPlayer(this.targetTeam).hand.updatePositions();
                    } else if (this.status.equals(CardStatus.DECK)) {
                        card.board.getPlayer(this.targetTeam).deck.updatePositions();
                    }
                }
            }
            card.status = this.status;
        }
    }

    @Override
    public void undo() {
        for (int i = this.c.size() - 1; i >= 0; i--) {
            Card card = this.c.get(i);
            Player p = card.board.getPlayer(card.team); // current player
            switch (card.status) { // removing from
            case HAND:
                p.hand.cards.remove(card);
                p.hand.updatePositions();
                break;
            case BOARD:
                card.board.removeBoardObject((BoardObject) card);
                break;
            case DECK:
                p.deck.cards.remove(card);
                p.deck.updatePositions();
                break;
            case LEADER:
                // wait
                break;
            default:
                break;
            }
            // goes against flow
            for (Effect e : this.prevEffects.get(i)) {
                card.addEffect(false, e);
            }
            List<Effect> basicEffects = card.getEffects(true);
            for (int j = 0; j < basicEffects.size(); j++) {
                basicEffects.get(j).mute = this.prevMute.get(i).get(j);
            }
            if (card instanceof BoardObject) {
                ((BoardObject) card).lastBoardPos = this.prevLastBoardPos.get(i);
            }
            if (card instanceof Minion) {
                ((Minion) card).health = this.prevHealth.get(i);
                ((Minion) card).attacksThisTurn = this.prevAttacks.get(i);
                ((Minion) card).summoningSickness = this.prevSick.get(i);
            }
            card.team = this.prevTeam.get(i);
            card.status = this.prevStatus.get(i);
            p = card.board.getPlayer(card.team); // old player
            switch (this.prevStatus.get(i)) { // adding to
            case HAND:
                p.hand.cards.add(this.prevPos.get(i), card);
                p.hand.updatePositions();
                break;
            case BOARD:
                card.board.addBoardObject((BoardObject) card, card.team, this.prevPos.get(i));
                break;
            case DECK:
                p.deck.cards.add(this.prevPos.get(i), card);
                p.deck.updatePositions();
                break;
            case LEADER:
                // wait
                break;
            default:
                break;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id + " " + this.c.size() + " " + this.status.toString() + " " + this.targetTeam + " ");
        for (int i = 0; i < this.c.size(); i++) {
            builder.append(this.c.get(i).toReference() + this.pos.get(i) + " ");
        }
        return builder.append("\n").toString();
    }

    public static EventPutCard fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        String sStatus = st.nextToken();
        CardStatus csStatus = null;
        for (CardStatus cs : CardStatus.values()) {
            if (cs.toString().equals(sStatus)) {
                csStatus = cs;
            }
        }
        int targetteam = Integer.parseInt(st.nextToken());
        ArrayList<Card> c = new ArrayList<Card>();
        ArrayList<Integer> pos = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            Card card = Card.fromReference(b, st);
            c.add(card);
            int poss = Integer.parseInt(st.nextToken());
            pos.add(poss);
        }
        return new EventPutCard(c, csStatus, targetteam, pos, null);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
