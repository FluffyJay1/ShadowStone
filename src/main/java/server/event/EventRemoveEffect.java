package server.event;

import java.util.*;

import client.Game;
import server.*;
import server.card.*;
import server.card.effect.*;

public class EventRemoveEffect extends Event {
    public static final int ID = 22;
    public final List<Effect> effects;
    private List<Integer> prevPos;
    private List<Integer> oldHealth;
    private List<Boolean> oldRemoved;
    private List<Boolean> oldAlive;
    final List<Card> markedForDeath;

    public EventRemoveEffect(List<Effect> effects, List<Card> markedForDeath) {
        super(ID);
        this.effects = effects;
        this.markedForDeath = Objects.requireNonNullElseGet(markedForDeath, ArrayList::new);
    }

    @Override
    public void resolve() {
        this.prevPos = new ArrayList<>(this.effects.size());
        this.oldHealth = new ArrayList<>(this.effects.size());
        this.oldRemoved = new ArrayList<>(this.effects.size());
        this.oldAlive = new ArrayList<>(this.effects.size());
        for (int i = 0; i < this.effects.size(); i++) {
            Effect e = this.effects.get(i);
            this.prevPos.add(e.getIndex());
            this.oldHealth.add(0);
            this.oldRemoved.add(e.removed);
            this.oldAlive.add(e.owner.alive);
            if (!e.removed) {
                Card c = e.owner;
                c.removeEffect(e, false);
                if (c instanceof Minion) {
                    Minion m = ((Minion) c);
                    this.oldHealth.set(i, m.health);
                    if (c.finalStatEffects.getStat(EffectStats.HEALTH) < m.health) {
                        m.health = m.finalStatEffects.getStat(EffectStats.HEALTH);
                    }
                    if (m.health <= 0 && c.alive) {
                        c.alive = false;
                        this.markedForDeath.add(m);
                    }
                }
                if (c.finalStatEffects.getUse(EffectStats.COUNTDOWN)
                        && c.finalStatEffects.getStat(EffectStats.COUNTDOWN) <= 0 && c.alive) {
                    c.alive = false;
                    this.markedForDeath.add(c);
                }
            }
        }
    }

    @Override
    public void undo() {
        for (int i = this.effects.size() - 1; i >= 0; i--) {
            Effect e = this.effects.get(i);
            Card c = e.owner;
            if (!this.oldRemoved.get(i)) {
                c.addEffect(false, this.prevPos.get(i), e);
                c.alive = this.oldAlive.get(i);
                if (c instanceof Minion) {
                    Minion m = (Minion) c;
                    m.health = this.oldHealth.get(i);
                }
            }
        }

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.effects.size()).append(" ");
        for (Effect e : this.effects) {
            builder.append(e.toReference());
        }
        builder.append(Game.EVENT_END);
        return builder.toString();
    }

    public static EventRemoveEffect fromString(Board b, StringTokenizer st) {
        int numEffects = Integer.parseInt(st.nextToken());
        List<Effect> effects = new ArrayList<>(numEffects);
        for (int i = 0; i < numEffects; i++) {
            effects.add(Effect.fromReference(b, st));
        }
        return new EventRemoveEffect(effects, null);
    }

    @Override
    public boolean conditions() {
        return this.effects.size() > 0;
    }
}
