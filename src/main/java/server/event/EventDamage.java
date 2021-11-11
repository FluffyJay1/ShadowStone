package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.Effect;

/*
 * Event alone may cause the board to enter an invalid state by killing minions,
 * requires markedForDeath cards to be killed later by the Resolver
 */
public class EventDamage extends Event {
    // whenever damage is dealt
    public static final int ID = 3;
    public List<Integer> damage;
    public List<Minion> m;
    public List<Boolean> poisonous;
    private List<Integer> oldHealth;
    private List<Boolean> oldAlive;
    public List<Card> markedForDeath;
    public Card cardSource; // used for animation probably (relied on for minion attack)
    public Effect effectSource; // used for animation probably, may be null
    public boolean minionAttack; // flags that this damage is from a minion's attack and should be treated differently

    public EventDamage(Card source, List<Minion> m, List<Integer> damage, List<Boolean> poisonous, boolean minionAttack,
            List<Card> markedForDeath) {
        super(ID);
        this.cardSource = source;
        this.m = m;
        this.damage = damage;
        this.poisonous = poisonous;
        this.markedForDeath = markedForDeath;
        this.minionAttack = minionAttack;
    }
    
    public EventDamage(Effect source, List<Minion> m, List<Integer> damage, List<Boolean> poisonous, List<Card> markedForDeath) {
        this(source.owner, m, damage, poisonous, false, markedForDeath);
        this.effectSource = source;
    }

    @Override
    public void resolve() {
        if (this.markedForDeath == null) {
            this.markedForDeath = new ArrayList<>();
        }
        this.oldHealth = new ArrayList<Integer>();
        this.oldAlive = new ArrayList<>();
        for (int i = 0; i < this.m.size(); i++) { // sure
            Minion minion = m.get(i);
            this.oldHealth.add(minion.health);
            this.oldAlive.add(minion.alive);
            if ((this.poisonous.get(i) && this.damage.get(i) > 0 && !(minion instanceof Leader))
                    || (minion.health > 0 && minion.health <= damage.get(i))) {
                // TODO poison immunity
                minion.alive = false;
                this.markedForDeath.add(minion);
            }
            minion.health -= damage.get(i);
        }
    }

    @Override
    public void undo() {
        for (int i = 0; i < this.m.size(); i++) { // sure
            Minion minion = m.get(i);
            minion.health = this.oldHealth.get(i);
            minion.alive = this.oldAlive.get(i);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.minionAttack).append(" ").append(Card.referenceOrNull(this.cardSource)).append(Effect.referenceOrNull(this.effectSource))
            .append(this.m.size()) .append(" ");
        for (int i = 0; i < this.m.size(); i++) {
            builder.append(this.m.get(i).toReference() + this.damage.get(i) + " " + this.poisonous.get(i) + " ");
        }
        builder.append("\n");
        return builder.toString();
    }

    public static EventDamage fromString(Board b, StringTokenizer st) {
        boolean minionAttack = Boolean.parseBoolean(st.nextToken());
        Card cardSource = Card.fromReference(b, st);
        Effect effectSource = Effect.fromReference(b, st);
        int size = Integer.parseInt(st.nextToken());
        ArrayList<Minion> m = new ArrayList<Minion>(size);
        ArrayList<Integer> damage = new ArrayList<Integer>(size);
        ArrayList<Boolean> poisonous = new ArrayList<Boolean>(size);
        for (int i = 0; i < size; i++) {
            Minion minion = (Minion) Card.fromReference(b, st);
            int d = Integer.parseInt(st.nextToken());
            boolean po = Boolean.parseBoolean(st.nextToken());
            m.add(minion);
            damage.add(d);
            poisonous.add(po);
        }
        EventDamage ret = new EventDamage(cardSource, m, damage, poisonous, minionAttack, null);
        ret.effectSource = effectSource;
        return ret;
    }

    @Override
    public boolean conditions() {
        return !this.m.isEmpty();
    }
}
