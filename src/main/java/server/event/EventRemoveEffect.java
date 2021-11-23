package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventRemoveEffect extends Event {
    public static final int ID = 22;
    public List<Effect> effects;
    private List<Integer> prevPos;
    private List<Integer> oldHealth;
    List<Card> markedForDeath;

    public EventRemoveEffect(List<Effect> effects, List<Card> markedForDeath) {
        super(ID);
        this.effects = effects;
        this.markedForDeath = Objects.requireNonNullElseGet(markedForDeath, ArrayList::new);
    }

    @Override
    public void resolve() {
        this.prevPos = new ArrayList<>();
        this.oldHealth = new ArrayList<>();
        for (int i = 0; i < this.effects.size(); i++) {
            Effect e = this.effects.get(i);
            this.prevPos.add(e.pos);
            this.oldHealth.add(0);
            Card c = e.owner;
            c.removeEffect(e);
            if (c instanceof Minion) {
                Minion m = ((Minion) c);
                this.oldHealth.set(i, m.health);
                if (c.finalStatEffects.getStat(EffectStats.HEALTH) < m.health) {
                    m.health = m.finalStatEffects.getStat(EffectStats.HEALTH);
                }
                if (m.health <= 0) {
                    this.markedForDeath.add(m);
                }
            }
            if (c.finalStatEffects.getUse(EffectStats.COUNTDOWN)
                    && c.finalStatEffects.getStat(EffectStats.COUNTDOWN) <= 0) {
                this.markedForDeath.add(c);
            }
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < this.effects.size(); i++) {
            Effect e = this.effects.get(i);
            Card c = e.owner;
            c.addEffect(false, this.prevPos.get(i), e);
            if (c instanceof Minion) {
                Minion m = (Minion) c;
                m.health = this.oldHealth.get(i);
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
        builder.append("\n");
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
