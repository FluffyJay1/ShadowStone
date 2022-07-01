package server.event;

import java.util.*;

import client.Game;
import client.ui.game.visualboardanimation.eventanimation.damage.EventAnimationDamage;
import org.jetbrains.annotations.NotNull;
import server.*;
import server.card.*;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.card.effect.Stat;

/*
 * Event alone may cause the board to enter an invalid state by killing minions,
 * requires markedForDeath cards to be killed later by the Resolver
 */
public class EventDamage extends Event {
    // whenever damage is dealt
    public static final int ID = 3;
    public final List<Integer> damage;
    public final List<Minion> m;
    private List<Integer> oldHealth;
    private List<Boolean> oldAlive;
    private List<Effect> addedEffects;
    public final List<Card> markedForDeath;
    public final List<Integer> actualDamage;
    public final Card cardSource; // used for animation probably (relied on for minion attack)
    public Effect effectSource; // used for animation probably, may be null

    // not proud of incorporating client animation logic into serverside game logic, but it's what we have to do
    public final String animationString;

    public EventDamage(Card source, List<Minion> m, List<Integer> damage,
            List<Card> markedForDeath, @NotNull String animationString) {
        super(ID);
        this.cardSource = source;
        this.m = m;
        this.damage = damage;
        this.markedForDeath = Objects.requireNonNullElseGet(markedForDeath, ArrayList::new);
        this.animationString = animationString;
        this.actualDamage = new ArrayList<>(this.damage.size());
    }

    public EventDamage(Effect source, List<Minion> m, List<Integer> damage, List<Card> markedForDeath, @NotNull String animationString) {
        this(source.owner, m, damage, markedForDeath, animationString);
        this.effectSource = source;
    }

    @Override
    public void resolve(Board b) {
        this.oldHealth = new ArrayList<>(this.m.size());
        this.oldAlive = new ArrayList<>(this.m.size());
        this.addedEffects = new ArrayList<>(this.m.size() * 3);
        boolean poisonous = this.cardSource.finalStats.get(Stat.POISONOUS) > 0;
        for (int i = 0; i < this.m.size(); i++) { // sure
            Minion minion = this.m.get(i);
            this.oldHealth.add(minion.health);
            this.oldAlive.add(minion.alive);
            this.actualDamage.add(0);
            int shield = minion.finalStats.get(Stat.SHIELD);
            if (shield > 0) {
                // negate damage, reduce shield
                int reduction = Math.min(this.damage.get(i), shield);
                Effect shieldRemover = new Effect("", EffectStats.builder()
                        .change(Stat.SHIELD, -reduction)
                        .build());
                minion.addEffect(false, shieldRemover);
                this.addedEffects.add(shieldRemover);
            } else {
                // normal damage processing
                if ((poisonous && minion.finalStats.get(Stat.STALWART) == 0 && this.damage.get(i) > 0 && !(minion instanceof Leader))
                        || (minion.health > 0 && minion.health <= this.damage.get(i)) && minion.alive) {
                    minion.alive = false;
                    this.markedForDeath.add(minion);
                }
                minion.health -= this.damage.get(i);
                this.actualDamage.set(i, this.damage.get(i));
                if (this.damage.get(i) > 0 && this.cardSource.finalStats.get(Stat.FREEZING_TOUCH) > 0) {
                    Effect frozen = new Effect("", EffectStats.builder()
                            .set(Stat.FROZEN, 1)
                            .build());
                    minion.addEffect(false, frozen);
                    this.addedEffects.add(frozen);
                }
            }
        }
        if (this.cardSource.finalStats.get(Stat.STEALTH) > 0) {
            Effect stealthRemover = new Effect("", EffectStats.builder()
                    .set(Stat.STEALTH, 0)
                    .build());
            this.cardSource.addEffect(false, stealthRemover);
            this.addedEffects.add(stealthRemover);
        }
    }

    @Override
    public void undo(Board b) {
        for (int i = this.m.size() - 1; i >= 0; i--) { // sure
            Minion minion = this.m.get(i);
            minion.health = this.oldHealth.get(i);
            minion.alive = this.oldAlive.get(i);
        }
        for (Effect e : this.addedEffects) {
            e.owner.removeEffect(e, true);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.id).append(" ").append(this.animationString)
                .append(Card.referenceOrNull(this.cardSource)).append(Effect.referenceOrNull(this.effectSource))
                .append(this.m.size()).append(" ");
        for (int i = 0; i < this.m.size(); i++) {
            builder.append(this.m.get(i).toReference()).append(this.damage.get(i)).append(" ");
        }
        builder.append(Game.EVENT_END);
        return builder.toString();
    }

    public static EventDamage fromString(Board b, StringTokenizer st) {
        // no reflection yet
        String animString = EventAnimationDamage.extractAnimationString(st);
        Card cardSource = Card.fromReference(b, st);
        Effect effectSource = Effect.fromReference(b, st);
        int size = Integer.parseInt(st.nextToken());
        ArrayList<Minion> m = new ArrayList<>(size);
        ArrayList<Integer> damage = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Minion minion = (Minion) Card.fromReference(b, st);
            int d = Integer.parseInt(st.nextToken());
            m.add(minion);
            damage.add(d);
        }
        EventDamage ret = new EventDamage(cardSource, m, damage, null, animString);
        ret.effectSource = effectSource;
        return ret;
    }

    @Override
    public boolean conditions() {
        return true;
    }
}
