package server.event;

import java.util.*;
import java.util.stream.IntStream;

import client.*;
import server.*;
import server.card.*;
import server.card.effect.Effect;

// Changes references, should not run concurrent with other events
public class EventCreateCard extends Event {
    public static final int ID = 2;
    public final List<Card> cards;
    public final CardStatus status;
    public final int team;
    public final List<Integer> cardpos;
    public final CardVisibility visibility;
    private UnleashPower prevUP;
    private Leader prevLeader;
    private int prevEpoch;
    private int prevShadows;
    public final List<Boolean> successful;
    public final List<Card> successfullyCreatedCards;
    final List<BoardObject> cardsEnteringPlay = new ArrayList<>();
    public final List<Card> markedForDeath;

    public EventCreateCard(List<Card> cards, int team, CardStatus status, List<Integer> cardpos, CardVisibility visibility) {
        super(ID);
        this.cards = cards;
        this.team = team;
        this.status = status;
        this.cardpos = cardpos;
        this.successful = new ArrayList<>(cards.size());
        this.successfullyCreatedCards = new ArrayList<>(cards.size());
        this.visibility = visibility;
        for (Card c : this.cards) {
            c.team = team;
            c.visibility = visibility;
        }
        this.markedForDeath = new ArrayList<>(cards.size());
    }

    @Override
    public void resolve(Board b) {
        Player p = b.getPlayer(this.team);
        if (this.cards.size() > 0 && this.status.equals(CardStatus.BOARD)) {
            this.prevEpoch = p.getPlayArea().getCurrentEpoch();
        }
        this.prevShadows = p.shadows;
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            int cardpos = this.cardpos.get(i);
            c.team = this.team;
            c.status = this.status;
            switch (this.status) {
                case HAND -> {
                    if (p.getHand().size() < p.maxHandSize) {
                        p.getHand().add(cardpos, c);
                        this.successfullyCreate(b, c);
                    } else {
                        c.alive = false;
                        p.shadows++;
                        this.successful.add(false);
                    }
                }
                case BOARD -> {
                    if (c instanceof BoardObject && p.getPlayArea().size() < p.maxPlayAreaSize) {
                        BoardObject bo = (BoardObject) c;
                        this.cardsEnteringPlay.add(bo);
                        p.getPlayArea().add(cardpos, bo);
                        if (c instanceof Minion) {
                            ((Minion) c).summoningSickness = true;
                        }
                        this.successfullyCreate(b, c);
                        if (bo.team == b.getLocalteam() && b instanceof PendingPlayPositioner) {
                            ((PendingPlayPositioner) b).getPendingPlayPositionProcessor().processOp(bo.getIndex(), null, true);
                        }
                    } else {
                        this.successful.add(false);
                    }
                }
                case DECK -> {
                    p.getDeck().add(cardpos, c);
                    this.successfullyCreate(b, c);
                }
                case UNLEASHPOWER -> {
                    this.prevUP = p.getUnleashPower().orElse(null);
                    if (this.prevUP != null) {
                        this.prevUP.status = CardStatus.GRAVEYARD;
                        p.getGraveyard().add(this.prevUP);
                    }
                    b.getPlayer(this.team).setUnleashPower((UnleashPower) c);
                    this.successfullyCreate(b, c);
                }
                case LEADER -> {
                    this.prevLeader = p.getLeader().orElse(null);
                    p.setLeader((Leader) c);
                    this.successfullyCreate(b, c);
                }
                default -> this.successful.add(false);
            }
            if (this.successful.get(i)) {
                if (b instanceof ServerBoard) {
                    ServerBoard sb = (ServerBoard) b;
                    c.getFinalEffects(false).forEach(sb::registerNewEffect);
                }
                EventCommon.markForDeathIfRequired(c, markedForDeath);
            }
            if (b instanceof ClientBoard) {
                ((ClientBoard) b).cardsCreated.add(c);
            }
        }
    }

    private void successfullyCreate(Board b, Card c) {
        this.successful.add(true);
        this.successfullyCreatedCards.add(c);
        c.setRef(b.cardTable.size());
        b.cardTable.add(c);
    }

    @Override
    public void undo(Board b) {
        Player p = b.getPlayer(this.team);
        for (int i = this.cards.size() - 1; i >= 0; i--) {
            Card c = this.cards.get(i);
            CardStatus status = c.status;
            if (this.successful.get(i)) {
                if (b instanceof ServerBoard) {
                    ServerBoard sb = (ServerBoard) b;
                    c.getFinalEffects(false).forEach(sb::unregisterEffect);
                }
                switch (status) {
                    case HAND -> p.getHand().remove(c);
                    case BOARD -> {
                        if (c instanceof BoardObject) {
                            p.getPlayArea().remove((BoardObject) c);
                        }
                    }
                    case DECK -> p.getDeck().remove(c);
                    case UNLEASHPOWER -> {
                        if (this.prevUP != null) {
                            p.getGraveyard().remove(this.prevUP);
                            this.prevUP.status = CardStatus.UNLEASHPOWER;
                        }
                        b.getPlayer(this.team).setUnleashPower(this.prevUP);
                    }
                    case LEADER -> b.getPlayer(this.team).setLeader(this.prevLeader);
                }
                b.cardTable.remove(c.getRef());
            }
        }
        if (this.cards.size() > 0 && this.status.equals(CardStatus.BOARD)) {
            p.getPlayArea().resetHistoryToEpoch(this.prevEpoch);
        }
        p.shadows = this.prevShadows;
    }

    @Override
    public List<BoardObject> cardsEnteringPlay() {
        return this.cardsEnteringPlay;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.visibility.name()).append(" ").append(this.cards.size()).append(" ");
        for (Card c : this.cards) {
            builder.append(c.toTemplateString());
        }
        builder.append(this.team).append(" ").append(this.status.toString());
        for (Integer i : this.cardpos) {
            builder.append(" ").append(i);
        }
        builder.append(Game.EVENT_END);
        return builder.toString();
    }

    public static EventCreateCard fromString(Board b, StringTokenizer st) {
        CardVisibility visibility = CardVisibility.valueOf(st.nextToken());
        int numCards = Integer.parseInt(st.nextToken());
        List<Card> cards = new ArrayList<>(numCards);
        for (int i = 0; i < numCards; i++) {
            Card c = Card.fromTemplateString(b, st);
            c.visibility = visibility;
            cards.add(c);
            if (b instanceof VisualBoard) {
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
        return new EventCreateCard(cards, team, csStatus, cardpos, visibility);
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
