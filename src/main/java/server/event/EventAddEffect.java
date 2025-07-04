package server.event;

import java.util.*;

import client.Game;
import server.*;
import server.card.*;
import server.card.effect.*;

/*
 * Event alone may cause the board to enter an invalid state by killing cards,
 * requires markedForDeath cards to be killed later by the Resolver
 */
public class EventAddEffect extends Event {
    public static final int ID = 1;
    public final List<? extends Card> c;
    final Effect e;
    public final List<Effect> effects;
    public final List<Boolean> successful;
    private List<Integer> oldHealth;
    private List<Boolean> oldAlive;
    public final List<Card> markedForDeath;

    public EventAddEffect(List<? extends Card> c, Effect e, List<Card> markedForDeath) {
        super(ID);
        this.c = c;
        this.e = e;
        this.markedForDeath = Objects.requireNonNullElseGet(markedForDeath, ArrayList::new);
        this.effects = new ArrayList<>(c.size());
        this.successful = new ArrayList<>(c.size());
    }

    @Override
    public void resolve(Board b) {
        this.oldHealth = new ArrayList<>(this.c.size());
        this.oldAlive = new ArrayList<>(this.c.size());
        for (int i = 0; i < this.c.size(); i++) {
            Card c = this.c.get(i);
            Effect clonede = null;
            try {
                clonede = e.clone();
            } catch (CloneNotSupportedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            this.successful.add(c.addEffect(false, clonede));
            if (this.successful.get(i) && b instanceof ServerBoard) {
                ServerBoard sb = (ServerBoard) b;
                sb.registerNewEffect(clonede);
            }
            this.effects.add(clonede);
            this.oldHealth.add(0);
            this.oldAlive.add(c.alive);
            if (c instanceof Minion) {
                Minion m = ((Minion) c);
                this.oldHealth.set(i, m.health);
                EventCommon.adjustMinionHealthAfterAddingEffect(m, clonede);
            }
            EventCommon.markForDeathIfRequired(c, markedForDeath);
        }
    }

    @Override
    public void undo(Board b) {
        for (int i = this.c.size() - 1; i >= 0; i--) {
            Card c = this.c.get(i);
            c.removeEffect(this.effects.get(i), true);
            if (this.successful.get(i) && b instanceof ServerBoard) {
                ServerBoard sb = (ServerBoard) b;
                sb.unregisterEffect(this.effects.get(i));
            }
            if (c instanceof Minion) {
                Minion m = ((Minion) c);
                m.health = this.oldHealth.get(i);
            }
            c.alive = this.oldAlive.get(i);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.c.size()).append(" ").append(e.toString());
        for (Card card : this.c) {
            builder.append(card.toReference());
        }
        builder.append(Game.EVENT_END);
        return builder.toString();
    }

    public static EventAddEffect fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        Effect e = Effect.fromString(b, st);
        ArrayList<Card> c = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Card card = Card.fromReference(b, st);
            c.add(card);
        }
        return new EventAddEffect(c, e, null);
    }

    @Override
    public boolean conditions() {
        return !this.c.isEmpty() && this.e != null;
    }
}
