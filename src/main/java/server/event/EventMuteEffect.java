package server.event;

import java.util.*;
import java.util.stream.Collectors;

import client.Game;
import server.*;
import server.card.*;
import server.card.effect.*;

public class EventMuteEffect extends Event {
    public static final int ID = 29;
    public final List<? extends Card> cards;
    final boolean mute;
    private List<List<Boolean>> prevMute;
    private List<Integer> prevHealth;
    private List<Boolean> prevAlive;
    final List<Card> markedForDeath;

    public EventMuteEffect(List<? extends Card> cards, boolean mute, List<Card> markedForDeath) {
        super(ID);
        this.cards = cards;
        this.mute = mute;
        this.markedForDeath = Objects.requireNonNullElseGet(markedForDeath, ArrayList::new);
    }

    @Override
    public void resolve(Board b) {
        this.prevMute = new ArrayList<>(this.cards.size());
        this.prevHealth = new ArrayList<>(this.cards.size());
        this.prevAlive = new ArrayList<>(this.cards.size());
        for (int i = 0; i < this.cards.size(); i++) {
            Card c = this.cards.get(i);
            List<Boolean> cardPrevMute = new ArrayList<>((int) c.getFinalEffects(false).count());
            this.prevMute.add(cardPrevMute);
            this.prevHealth.add(0);
            this.prevAlive.add(c.alive);
            List<Effect> allEffects = c.getFinalEffects(false).collect(Collectors.toList());
            for (Effect e : allEffects) {
                cardPrevMute.add(e.mute);
                c.muteEffect(e, this.mute);
            }
            if (c instanceof Minion) {
                Minion m = ((Minion) c);
                this.prevHealth.set(i, m.health);
                if (m.finalStats.get(Stat.HEALTH) < m.health) {
                    m.health = m.finalStats.get(Stat.HEALTH);
                }
                if (m.health <= 0 && m.alive) {
                    m.alive = false;
                    this.markedForDeath.add(m);
                }
            }
            if (c.finalStats.contains(Stat.COUNTDOWN)
                    && c.finalStats.get(Stat.COUNTDOWN) <= 0 && c.alive) {
                c.alive = false;
                this.markedForDeath.add(c);
            }
        }
    }

    @Override
    public void undo(Board b) {
        for (int i = this.cards.size() - 1; i >= 0; i--) {
            Card c = this.cards.get(i);
            List<Effect> allEffects = c.getFinalEffects(false).collect(Collectors.toList());
            List<Boolean> cardPrevMute = this.prevMute.get(i);
            for (int j = 0; j < allEffects.size(); j++) {
                Effect e = allEffects.get(j);
                c.muteEffect(e, cardPrevMute.get(j));
            }
            c.alive = this.prevAlive.get(i);
            if (c instanceof Minion) {
                Minion m = ((Minion) c);
                m.health = this.prevHealth.get(i);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.id).append(" ").append(this.mute).append(" ").append(this.cards.size()).append(" ");
        for (Card c : this.cards) {
            sb.append(c.toReference());
        }
        sb.append(Game.EVENT_END);
        return sb.toString();
    }

    public static EventMuteEffect fromString(Board b, StringTokenizer st) {
        boolean mute = Boolean.parseBoolean(st.nextToken());
        int size = Integer.parseInt(st.nextToken());
        List<Card> cards = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            cards.add(Card.fromReference(b, st));
        }
        return new EventMuteEffect(cards, mute, null);
    }

    @Override
    public boolean conditions() {
        return !this.cards.isEmpty();
    }
}
