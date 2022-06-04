package server.event;

import java.util.*;

import client.Game;
import client.PendingPlayPositioner;
import server.*;
import server.card.*;
import server.card.effect.*;

public class EventPutCard extends Event {
    // for effects that put specific cards in hand or just draw cards
    public static final int ID = 12;
    public final List<? extends Card> cards;
    public final List<Integer> pos; // pos == -1 means last
    public final CardStatus status;
    private boolean play;
    final int targetTeam;
    private List<CardStatus> prevStatus;
    private List<List<Effect>> prevEffects;
    private List<List<Boolean>> prevMute;
    private List<Integer> prevPos;
    private List<Integer> prevLastBoardPos;
    private List<Integer> prevLastBoardEpoch;
    private List<Integer> prevTeam;
    private List<Integer> prevHealth;
    private List<Integer> prevAttacks;
    private int prevEpoch1, prevEpoch2;
    private List<Boolean> prevSick;
    private List<Boolean> oldAlive;
    private List<Integer> prevSpellboosts;
    public List<Boolean> attempted; // i.e. removed from original position
    public List<Boolean> successful; // i.e. added to intended position i.e. not killed
    final List<BoardObject> cardsEnteringPlay = new ArrayList<>();
    final List<BoardObject> cardsLeavingPlay = new ArrayList<>();
    List<Card> markedForDeath;

    public EventPutCard(List<? extends Card> c, CardStatus status, int team, List<Integer> pos, boolean play, List<Card> markedForDeath) {
        super(ID);
        this.cards = c;
        this.status = status;
        this.targetTeam = team;
        this.pos = pos;
        this.play = play;
        this.markedForDeath = markedForDeath;
    }

    @Override
    public void resolve(Board b) {
        if (this.markedForDeath == null) {
            this.markedForDeath = new ArrayList<>();
        }
        this.prevStatus = new ArrayList<>(this.cards.size());
        this.prevEffects = new ArrayList<>(this.cards.size());
        this.prevMute = new ArrayList<>(this.cards.size());
        this.prevPos = new ArrayList<>(this.cards.size());
        this.prevLastBoardPos = new ArrayList<>(this.cards.size());
        this.prevLastBoardEpoch = new ArrayList<>(this.cards.size());
        this.prevTeam = new ArrayList<>(this.cards.size());
        this.prevHealth = new ArrayList<>(this.cards.size());
        this.prevAttacks = new ArrayList<>(this.cards.size());
        this.prevSick = new ArrayList<>(this.cards.size());
        this.oldAlive = new ArrayList<>(this.cards.size());
        this.prevSpellboosts = new ArrayList<>(this.cards.size());
        this.attempted = new ArrayList<>(this.cards.size());
        this.successful = new ArrayList<>(this.cards.size());
        this.prevEpoch1 = b.getPlayer(1).getPlayArea().getCurrentEpoch();
        this.prevEpoch2 = b.getPlayer(-1).getPlayArea().getCurrentEpoch();
        for (int i = 0; i < this.cards.size(); i++) {
            Card card = this.cards.get(i);
            this.prevStatus.add(card.status);
            this.prevEffects.add(new LinkedList<>());
            this.prevMute.add(new LinkedList<>());
            this.prevPos.add(card.getIndex());
            this.prevLastBoardPos.add(0);
            this.prevLastBoardEpoch.add(0);
            this.prevTeam.add(card.team);
            this.prevHealth.add(0);
            this.prevAttacks.add(0);
            this.prevSick.add(true);
            this.oldAlive.add(card.alive);
            this.prevSpellboosts.add(card.spellboosts);
            this.attempted.add(false);
            this.successful.add(false);
            if (card instanceof BoardObject) {
                BoardObject bo = (BoardObject) card;
                this.prevLastBoardPos.set(i, bo.lastBoardPos);
                this.prevLastBoardEpoch.set(i, bo.lastBoardEpoch);
            }
            if (card instanceof Minion) {
                Minion m = (Minion) card;
                this.prevHealth.set(i, m.health);
                this.prevAttacks.set(i, m.attacksThisTurn);
                this.prevSick.set(i, m.summoningSickness);
            }
            Player sourceP = b.getPlayer(card.team);
            Player destP = b.getPlayer(this.targetTeam);
            if (!this.shouldAttemptMove(card, sourceP, destP)) {
                continue;
            }
            this.attempted.set(i, true);
            if (!this.willSucceedMove(card, sourceP, destP)) {
                if (card.alive) {
                    card.alive = false;
                    this.markedForDeath.add(card); // mill
                }
                continue;
            }
            this.successful.set(i, true);
            switch (card.status) { // removing from
                case HAND -> {
                    sourceP.getHand().remove(card);
                }
                case BOARD -> {
                    assert card instanceof BoardObject;
                    BoardObject bo = (BoardObject) card;
                    if (card.team == b.localteam && b instanceof PendingPlayPositioner) {
                        ((PendingPlayPositioner) b).getPendingPlayPositionProcessor().processOp(card.getIndex(), null, false);
                    }
                    sourceP.getPlayArea().remove(card);
                    if (!card.status.equals(CardStatus.BOARD)) {
                        bo.lastBoardPos = bo.getIndex();
                        bo.lastBoardEpoch = sourceP.getPlayArea().getCurrentEpoch();
                        this.cardsLeavingPlay.add(bo);
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
                card.spellboosts = 0;
                if (card instanceof Minion) {
                    ((Minion) card).health = card.finalStats.get(Stat.HEALTH);
                    ((Minion) card).attacksThisTurn = 0;
                }
            }
            card.team = this.targetTeam;
            // now adding to
            switch (this.status) {
                case BOARD -> {
                    if (card instanceof BoardObject) {
                        BoardObject bo = (BoardObject) card;
                        if (!card.status.equals(this.status)) {
                            this.cardsEnteringPlay.add(bo);
                        }
                        destP.getPlayArea().add(this.pos.get(i), bo);
                        if (card instanceof Minion) {
                            ((Minion) card).summoningSickness = true;
                        }
                        if (bo.team == b.localteam && b instanceof PendingPlayPositioner) {
                            BoardObject pendingObject = this.play ? bo : null;
                            ((PendingPlayPositioner) b).getPendingPlayPositionProcessor().processOp(bo.getIndex(), pendingObject, true);
                        }
                    }
                }
                case HAND -> destP.getHand().add(this.pos.get(i), card);
                case DECK -> destP.getDeck().add(this.pos.get(i), card);
                default -> System.err.println("uhm i don't know where to put this card");
            }
            card.status = this.status;
        }
    }

    @Override
    public void undo(Board b) {
        for (int i = this.cards.size() - 1; i >= 0; i--) {
            Card card = this.cards.get(i);
            card.alive = this.oldAlive.get(i);
            if (this.successful.get(i)) {
                Player p = b.getPlayer(card.team); // current player
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
                card.spellboosts = prevSpellboosts.get(i);
                if (card instanceof BoardObject) {
                    BoardObject bo = (BoardObject) card;
                    bo.lastBoardPos = this.prevLastBoardPos.get(i);
                    bo.lastBoardEpoch = this.prevLastBoardEpoch.get(i);
                }
                if (card instanceof Minion) {
                    Minion m = (Minion) card;
                    m.health = this.prevHealth.get(i);
                    m.attacksThisTurn = this.prevAttacks.get(i);
                    m.summoningSickness = this.prevSick.get(i);
                }
                card.team = this.prevTeam.get(i);
                card.status = this.prevStatus.get(i);
                p = b.getPlayer(card.team); // old player
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
        b.getPlayer(1).getPlayArea().resetHistoryToEpoch(this.prevEpoch1);
        b.getPlayer(-1).getPlayArea().resetHistoryToEpoch(this.prevEpoch2);
    }

    @Override
    public List<BoardObject> cardsEnteringPlay() {
        return this.cardsEnteringPlay;
    }

    @Override
    public List<BoardObject> cardsLeavingPlay() {
        return this.cardsLeavingPlay;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ")
                .append(this.cards.size()).append(" ")
                .append(this.status.toString()).append(" ")
                .append(this.targetTeam).append(" ")
                .append(this.play).append(" ");
        for (int i = 0; i < this.cards.size(); i++) {
            builder.append(this.cards.get(i).toReference()).append(this.pos.get(i)).append(" ");
        }
        return builder.append(Game.EVENT_END).toString();
    }

    public static EventPutCard fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        String sStatus = st.nextToken();
        CardStatus csStatus = CardStatus.valueOf(sStatus);
        int targetteam = Integer.parseInt(st.nextToken());
        boolean play = Boolean.parseBoolean(st.nextToken());
        ArrayList<Card> c = new ArrayList<>();
        ArrayList<Integer> pos = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Card card = Card.fromReference(b, st);
            c.add(card);
            int poss = Integer.parseInt(st.nextToken());
            pos.add(poss);
        }
        return new EventPutCard(c, csStatus, targetteam, pos, play, null);
    }

    // if we are moving to the board, we shouldn't attempt to move cards if they don't fit on the board
    private boolean shouldAttemptMove(Card c, Player sourceP, Player destP) {
        if (this.status.equals(CardStatus.BOARD)) {
            if (c.status.equals(CardStatus.BOARD) && sourceP.team == destP.team) {
                return true;
            }
            return destP.getPlayArea().size() < destP.maxPlayAreaSize;
        }
        return true;
    }

    // if we overfill the hand, destroy it instead
    private boolean willSucceedMove(Card c, Player sourceP, Player destP) {
        if (this.status.equals(CardStatus.HAND)) {
            if (c.status.equals(CardStatus.HAND) && sourceP.team == destP.team) {
                return true;
            }
            return destP.getHand().size() < destP.maxHandSize;
        }
        return true;
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
