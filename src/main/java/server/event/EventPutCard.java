package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventPutCard extends Event {
    // for effects that put specific cards in hand or just draw cards
    public static final int ID = 12;
    public final List<Card> c;
    public final List<Integer> pos; // pos == -1 means last
    public final CardStatus status;
    final int targetTeam;
    private List<CardStatus> prevStatus;
    private List<List<Effect>> prevEffects;
    private List<List<Boolean>> prevMute;
    private List<Integer> prevPos;
    private List<Integer> prevLastBoardPos;
    private List<Integer> prevTeam;
    private List<Integer> prevHealth;
    private List<Integer> prevAttacks;
    private List<Boolean> prevSick;
    private List<Boolean> oldAlive;
    final List<BoardObject> cardsEnteringPlay = new ArrayList<>();
    final List<BoardObject> cardsLeavingPlay = new ArrayList<>();
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
        this.prevStatus = new ArrayList<>(this.c.size());
        this.prevEffects = new ArrayList<>(this.c.size());
        this.prevMute = new ArrayList<>(this.c.size());
        this.prevPos = new ArrayList<>(this.c.size());
        this.prevLastBoardPos = new ArrayList<>(this.c.size());
        this.prevTeam = new ArrayList<>(this.c.size());
        this.prevHealth = new ArrayList<>(this.c.size());
        this.prevAttacks = new ArrayList<>(this.c.size());
        this.prevSick = new ArrayList<>(this.c.size());
        this.oldAlive = new ArrayList<>(this.c.size());
        for (int i = 0; i < this.c.size(); i++) {
            Card card = this.c.get(i);
            this.prevStatus.add(card.status);
            this.prevEffects.add(new LinkedList<>());
            this.prevMute.add(new LinkedList<>());
            this.prevPos.add(card.getIndex());
            this.prevLastBoardPos.add(0);
            this.prevTeam.add(card.team);
            this.prevHealth.add(0);
            this.prevAttacks.add(0);
            this.prevSick.add(true);
            this.oldAlive.add(card.alive);
            if (card instanceof BoardObject) {
                this.prevLastBoardPos.set(i, ((BoardObject) card).lastBoardPos);
            }
            if (card instanceof Minion) {
                this.prevHealth.set(i, ((Minion) card).health);
                this.prevAttacks.set(i, ((Minion) card).attacksThisTurn);
                this.prevSick.set(i, ((Minion) card).summoningSickness);
            }
            Player sourceP = card.board.getPlayer(card.team);
            switch (card.status) { // removing from
                case HAND -> {
                    sourceP.getHand().remove(card);
                }
                case BOARD -> {
                    assert card instanceof BoardObject;
                    sourceP.getPlayArea().remove(card);
                    if (!card.status.equals(this.status)) {
                        this.cardsLeavingPlay.add((BoardObject) card);
                    }
                }
                case DECK -> {
                    sourceP.getDeck().remove(card);
                }
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
            Player destP = card.board.getPlayer(this.targetTeam);
            card.team = this.targetTeam;
            // now adding to
            switch (this.status) {
                case BOARD -> {
                    if (card instanceof BoardObject) {
                        // TODO: CHECK FOR ROOM ON BOARD
                        BoardObject bo = (BoardObject) card;
                        if (!card.status.equals(this.status)) {
                            this.cardsEnteringPlay.add(bo);
                        }
                        destP.getPlayArea().add(this.pos.get(i), bo);
                        bo.lastBoardPos = bo.getIndex();
                        if (card instanceof Minion) {
                            ((Minion) card).summoningSickness = true;
                        }
                    }
                }
                case HAND -> {
                    if (destP.getHand().size() >= destP.maxHandSize) {
                        if (card.alive) {
                            card.alive = false;
                            this.markedForDeath.add(card); // mill
                        }
                    } else {
                        destP.getHand().add(this.pos.get(i), card);
                    }
                }
                case DECK -> {
                    destP.getHand().add(this.pos.get(i), card);
                }
                default -> {
                    System.err.println("uhm i don't know where to put this card");
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
                case HAND -> p.getHand().remove(card);
                case BOARD -> p.getPlayArea().remove((BoardObject) card);
                case DECK -> p.getDeck().remove(card);
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
            card.alive = this.oldAlive.get(i);
            p = card.board.getPlayer(card.team); // old player
            switch (this.prevStatus.get(i)) { // adding to
                case HAND -> p.getHand().add(this.prevPos.get(i), card);
                case BOARD -> {
                    assert card instanceof BoardObject;
                    p.getPlayArea().add(this.prevPos.get(i), (BoardObject) card);
                }
                case DECK -> p.getDeck().add(this.prevPos.get(i), card);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.c.size()).append(" ").append(this.status.toString()).append(" ").append(this.targetTeam).append(" ");
        for (int i = 0; i < this.c.size(); i++) {
            builder.append(this.c.get(i).toReference()).append(this.pos.get(i)).append(" ");
        }
        return builder.append("\n").toString();
    }

    public static EventPutCard fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        String sStatus = st.nextToken();
        CardStatus csStatus = CardStatus.valueOf(sStatus);
        int targetteam = Integer.parseInt(st.nextToken());
        ArrayList<Card> c = new ArrayList<>();
        ArrayList<Integer> pos = new ArrayList<>();
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
