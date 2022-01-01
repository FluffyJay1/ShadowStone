package server.event;

import java.util.*;

import server.*;

public class EventManaChange extends Event {
    public static final int ID = 6;
    final Player p;
    final int mana;
    final boolean empty;
    final boolean recover;
    private int prevMana, prevMaxMana;

    public EventManaChange(Player p, int mana, boolean empty, boolean recover) {
        super(ID);
        this.p = p;
        this.mana = mana;
        this.empty = empty;
        this.recover = recover;
    }

    @Override
    public void resolve() {
        this.prevMana = this.p.mana;
        this.prevMaxMana = this.p.maxmana;
        if (!this.recover) { // change max mana
            if (this.p.maxmana + this.mana > this.p.maxmaxmana) {
                this.p.maxmana = this.p.maxmaxmana;
            } else if (this.p.maxmana + this.mana < 0) {
                this.p.maxmana = 0;
            } else {
                this.p.maxmana += this.mana;
            }
        }
        if (!this.empty) { // change regular mana
            if (this.p.mana + this.mana > this.p.maxmana) {
                this.p.mana = this.p.maxmana;
            } else if (this.p.mana + this.mana < 0) {
                this.p.mana = 0;
            } else {
                this.p.mana += this.mana;
            }
        }
    }

    @Override
    public void undo() {
        this.p.mana = this.prevMana;
        this.p.maxmana = this.prevMaxMana;
    }

    @Override
    public String toString() {
        return this.id + " " + this.p.team + " " + this.mana + " " + this.empty + " " + this.recover + "\n";
    }

    public static EventManaChange fromString(Board b, StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        Player p = b.getPlayer(team);
        int mana = Integer.parseInt(st.nextToken());
        boolean empty = Boolean.parseBoolean(st.nextToken());
        boolean recover = Boolean.parseBoolean(st.nextToken());
        return new EventManaChange(p, mana, empty, recover);
    }

    @Override
    public boolean conditions() {
        return !(this.empty && this.recover);
    }
}
