package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventMuteEffect extends Event {
    public static final int ID = 29;
    public final Card c;
    final Effect e;
    final boolean mute;
    private boolean prevMute;
    private int prevHealth;
    private boolean oldAlive;
    final List<Card> markedForDeath;

    public EventMuteEffect(Card c, Effect e, boolean mute, List<Card> markedForDeath) {
        super(ID);
        this.c = c;
        this.e = e;
        this.mute = mute;
        this.markedForDeath = Objects.requireNonNullElseGet(markedForDeath, ArrayList::new);
    }

    @Override
    public void resolve() {
        this.prevMute = this.e.mute;
        this.oldAlive = this.c.alive;
        this.c.muteEffect(this.e, this.mute);
        if (this.c instanceof Minion) {
            Minion m = ((Minion) this.c);
            this.prevHealth = m.health;
            if (this.c.finalStatEffects.getStat(EffectStats.HEALTH) < m.health) {
                m.health = m.finalStatEffects.getStat(EffectStats.HEALTH);
            }
            if (m.health <= 0 && m.alive) {
                m.alive = false;
                this.markedForDeath.add(m);
            }
        }
        if (this.c.finalStatEffects.getUse(EffectStats.COUNTDOWN)
                && this.c.finalStatEffects.getStat(EffectStats.COUNTDOWN) <= 0 && this.c.alive) {
            this.c.alive = false;
            this.markedForDeath.add(this.c);
        }
    }

    @Override
    public void undo() {
        this.c.muteEffect(this.e, this.prevMute);
        this.c.alive = this.oldAlive;
        if (c instanceof Minion) {
            Minion m = ((Minion) c);
            m.health = this.prevHealth;
        }
    }

    @Override
    public String toString() {
        return this.id + " " + this.c.toReference() + this.e.toReference() + this.mute + "\n";
    }

    public static EventMuteEffect fromString(Board b, StringTokenizer st) {
        Card c = Card.fromReference(b, st);
        Effect e = Effect.fromReference(b, st);
        boolean mute = Boolean.parseBoolean(st.nextToken());
        return new EventMuteEffect(c, e, mute, null);
    }

    @Override
    public boolean conditions() {
        return this.c.getEffects(false).contains(this.e);
    }
}
