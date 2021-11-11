package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

/*
 * Event alone may cause the board to enter an invalid state by killing cards,
 * requires markedForDeath cards to be killed later by the Resolver
 */
public class EventAddEffect extends Event {
    public static final int ID = 1;
    public List<? extends Card> c = new ArrayList<>();
    Effect e;
    public List<Effect> effects;
    private List<Integer> oldHealth;
    private List<Boolean> oldAlive;
    public List<Card> markedForDeath;

    public EventAddEffect(List<? extends Card> c, Effect e, List<Card> markedForDeath) {
        super(ID);
        this.c = c;
        this.e = e;
        this.markedForDeath = markedForDeath;
    }

    public EventAddEffect(Card c, Effect e, List<Card> markedForDeath) {
        this(List.of(c), e, markedForDeath);
    }

    @Override
    public void resolve() {
        if (this.markedForDeath == null) {
            this.markedForDeath = new ArrayList<>();
        }
        this.effects = new ArrayList<Effect>();
        this.oldHealth = new ArrayList<Integer>();
        this.oldAlive = new ArrayList<>();
        for (int i = 0; i < this.c.size(); i++) {
            Card c = this.c.get(i);
            Effect clonede = null;
            try {
                clonede = e.clone();
            } catch (CloneNotSupportedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            c.addEffect(false, clonede);
            this.effects.add(clonede);
            this.oldHealth.add(0);
            this.oldAlive.add(c.alive);
            if (c instanceof Minion) {
                Minion m = ((Minion) c);
                this.oldHealth.set(i, m.health);
                if (e.set.use[EffectStats.HEALTH]) {
                    m.health = e.set.stats[EffectStats.HEALTH];
                }
                if (e.change.use[EffectStats.HEALTH] && e.change.stats[EffectStats.HEALTH] > 0) {
                    m.health += e.change.stats[EffectStats.HEALTH];
                }
                if (c.finalStatEffects.getStat(EffectStats.HEALTH) < m.health) {
                    m.health = m.finalStatEffects.getStat(EffectStats.HEALTH);
                }
                if (m.health <= 0) {
                    m.alive = false;
                    this.markedForDeath.add(m);
                }
            }
            if (c.finalStatEffects.getUse(EffectStats.COUNTDOWN)
                    && c.finalStatEffects.getStat(EffectStats.COUNTDOWN) <= 0) {
                c.alive = false;
                this.markedForDeath.add(c);
            }
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < this.c.size(); i++) {
            Card c = this.c.get(i);
            c.removeEffect(this.effects.get(i));
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
        builder.append(this.id + " " + this.c.size() + " " + e.toString());
        for (int i = 0; i < this.c.size(); i++) {
            builder.append(this.c.get(i).toReference());
        }
        builder.append("\n");
        return builder.toString();
    }

    public static EventAddEffect fromString(Board b, StringTokenizer st) {
        int size = Integer.parseInt(st.nextToken());
        Effect e = Effect.fromString(b, st);
        ArrayList<Card> c = new ArrayList<Card>();
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
