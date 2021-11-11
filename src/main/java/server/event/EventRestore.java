package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventRestore extends Event {
    public static final int ID = 13;
    // actualheal is (kind of) for display only
    public List<Integer> heal, actualHeal;
    public List<Minion> m;
    private List<Integer> oldHealth;
    public Effect source; // probably for animation purposes

    public EventRestore(Effect source, List<Minion> m, List<Integer> heal) {
        super(ID);
        this.source = source;
        this.m = m;
        this.heal = heal;
        this.actualHeal = new ArrayList<>(heal.size());
    }

    @Override
    public void resolve() {
        this.oldHealth = new ArrayList<Integer>();
        for (int i = 0; i < this.m.size(); i++) { // whatever
            Minion minion = this.m.get(i);
            this.oldHealth.add(minion.health);
            minion.health += this.heal.get(i);
            int healAmount = this.heal.get(i);
            if (minion.health > minion.finalStatEffects.getStat(EffectStats.HEALTH)) {
                healAmount -= minion.health - minion.finalStatEffects.getStat(EffectStats.HEALTH);
                minion.health = minion.finalStatEffects.getStat(EffectStats.HEALTH);
            }
            this.actualHeal.add(healAmount);
        }

        // TODO on healed
    }

    @Override
    public void undo() {
        for (int i = 0; i < this.m.size(); i++) {
            Minion minion = this.m.get(i);
            minion.health = this.oldHealth.get(i);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(Effect.referenceOrNull(this.source)).append(this.m.size())
                .append(" ");
        for (int i = 0; i < this.m.size(); i++) {
            builder.append(this.m.get(i).toReference() + this.heal.get(i) + " ");
        }
        return builder.append("\n").toString();
    }

    public static EventRestore fromString(Board b, StringTokenizer st) {
        Effect source = Effect.fromReference(b, st);
        int size = Integer.parseInt(st.nextToken());
        ArrayList<Minion> m = new ArrayList<>(size);
        ArrayList<Integer> heal = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Minion minion = (Minion) Card.fromReference(b, st);
            int h = Integer.parseInt(st.nextToken());
            m.add(minion);
            heal.add(h);
        }
        return new EventRestore(source, m, heal);
    }

    @Override
    public boolean conditions() {
        return !this.m.isEmpty();
    }
}
