package server.event;

import client.Game;
import server.Board;
import server.Player;
import server.card.effect.Effect;

import java.util.StringTokenizer;

// From Spend effects, different than EventManaChange because this gets a cool animation
public class EventSpend extends Event {
    public static final int ID = 20;

    public Effect source;
    public int amount;
    private int prevMana;

    public EventSpend(Effect source, int amount) {
        super(ID);
        this.source = source;
        this.amount = amount;
    }

    @Override
    public void resolve(Board b) {
        Player p = this.source.owner.player;
        this.prevMana = p.mana;
        if (p.mana >= this.amount) {
            p.mana -= this.amount;
        }
    }

    @Override
    public void undo(Board b) {
        Player p = this.source.owner.player;
        p.mana = this.prevMana;
    }

    @Override
    public String toString() {
        return this.id + " " + this.amount + " " + this.source.toReference() + Game.EVENT_END;
    }

    public static EventSpend fromString(Board b, StringTokenizer st) {
        int amount = Integer.parseInt(st.nextToken());
        Effect source = Effect.fromReference(b, st);
        return new EventSpend(source, amount);
    }
}
