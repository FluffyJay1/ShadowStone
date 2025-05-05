package server.event;

import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import client.Game;
import client.PendingPlayPositioner;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.*;
import server.card.*;
import server.card.effect.Stat;

// Changes references, should not run concurrent with other events
public class EventDestroy extends Event {
    // Shouldn't process this event outright, as it ignores lastwords triggers
    public static final int ID = 4;
    public final List<? extends Card> cards;
    public final Cause cause;
    private List<Boolean> alive;
    private List<CardStatus> prevStatus;
    private List<Integer> prevPos;
    private List<Integer> prevLastBoardPos;
    private List<Integer> prevLastBoardEpoch;
    private int prevEpoch1, prevEpoch2;
    private int prevShadows1, prevShadows2;
    public List<Boolean> successful;
    final List<BoardObject> cardsLeavingPlay = new ArrayList<>(); // required for listeners
    // not proud of incorporating client animation logic into serverside game logic, but it's what we have to do
    private final String animationString;

    public EventDestroy(List<? extends Card> c, Cause cause, @NotNull String animationString) {
        super(ID);
        this.cards = c;
        this.cause = cause;
        this.animationString = animationString;
    }

    public EventDestroy(List<? extends Card> c, Cause cause) {
        this(c, cause, EventAnimation.stringOrNull(null));
    }

    @Override
    public void resolve(Board b) {
        this.alive = new ArrayList<>(this.cards.size());
        this.prevStatus = new ArrayList<>(this.cards.size());
        this.prevPos = new ArrayList<>(this.cards.size());
        this.prevLastBoardPos = new ArrayList<>(this.cards.size());
        this.prevLastBoardEpoch = new ArrayList<>(this.cards.size());
        this.successful = new ArrayList<>(this.cards.size());
        this.prevEpoch1 = b.getPlayer(1).getPlayArea().getCurrentEpoch();
        this.prevEpoch2 = b.getPlayer(-1).getPlayArea().getCurrentEpoch();
        this.prevShadows1 = b.getPlayer(1).shadows;
        this.prevShadows2 = b.getPlayer(-1).shadows;
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            Player p = b.getPlayer(c.team);
            this.alive.add(c.alive);
            this.prevStatus.add(c.status);
            this.prevPos.add(c.getIndex());
            this.prevLastBoardPos.add(0);
            this.prevLastBoardEpoch.add(0);
            this.successful.add(false);
            if (c instanceof BoardObject) {
                BoardObject bo = (BoardObject) c;
                this.prevLastBoardPos.set(i, bo.lastBoardPos);
                this.prevLastBoardEpoch.set(i, bo.lastBoardEpoch);
            }
            if (!c.status.equals(CardStatus.GRAVEYARD) && !(this.cause.equals(Cause.EFFECT) && (c.finalStats.get(Stat.STALWART) > 0 || c.finalStats.get(Stat.INVULNERABLE) > 0))) {
                this.successful.set(i, true);
                if (!this.cause.equals(Cause.CAST)) {
                    p.shadows++;
                }
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
    public void undo(Board b) {
        for (int i = this.cards.size() - 1; i >= 0; i--) {
            Card c = this.cards.get(i);
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
                    BoardObject bo = (BoardObject) c;
                    bo.lastBoardPos = this.prevLastBoardPos.get(i);
                    bo.lastBoardEpoch = this.prevLastBoardEpoch.get(i);
                }
                c.status = status;
            }
        }
        b.getPlayer(1).getPlayArea().resetHistoryToEpoch(this.prevEpoch1);
        b.getPlayer(-1).getPlayArea().resetHistoryToEpoch(this.prevEpoch2);
        b.getPlayer(1).shadows = this.prevShadows1;
        b.getPlayer(-1).shadows = this.prevShadows2;
    }

    @Override
    public List<BoardObject> cardsLeavingPlay() {
        return this.cardsLeavingPlay;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.cause.name()).append(" ").append(this.animationString).append(this.cards.size()).append(" ");
        for (Card card : this.cards) {
            builder.append(card.toReference());
        }
        return builder.append(Game.EVENT_END).toString();
    }

    public static EventDestroy fromString(Board b, StringTokenizer st) {
        Cause cause = Cause.valueOf(st.nextToken());
        String animString = EventAnimation.extractAnimationString(st);
        int size = Integer.parseInt(st.nextToken());
        ArrayList<Card> c = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Card card = Card.fromReference(b, st);
            c.add(card);
        }
        return new EventDestroy(c, cause, animString);
    }

    @Override
    public boolean conditions() {
        return true;
    }

    @Override
    public @Nullable String getAnimationString() {
        return this.animationString;
    }

    public enum Cause {
        EFFECT, // can be blocked
        CAST, NATURAL // cannot be blocked
    }
}
