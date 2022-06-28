package server.event;

import java.util.*;

import client.Game;
import server.*;

public class EventManaChange extends Event {
    public static final int ID = 6;
    final Player p;
    final int mana;
    final boolean changeCurrent;
    final boolean changeMax;
    final boolean currentIgnoreMax;
    private int prevMana, prevMaxMana;

    public EventManaChange(Player p, int mana, boolean changeCurrent, boolean changeMax, boolean currentIgnoreMax) {
        super(ID);
        this.p = p;
        this.mana = mana;
        this.changeCurrent = changeCurrent;
        this.changeMax = changeMax;
        this.currentIgnoreMax = currentIgnoreMax;
    }

    @Override
    public void resolve(Board b) {
        this.prevMana = this.p.mana;
        this.prevMaxMana = this.p.maxmana;
        if (this.changeMax) { // change max mana
            if (this.p.maxmana + this.mana > this.p.maxmaxmana) {
                this.p.maxmana = this.p.maxmaxmana;
            } else if (this.p.maxmana + this.mana < 0) {
                this.p.maxmana = 0;
            } else {
                this.p.maxmana += this.mana;
            }
        }
        if (this.changeCurrent) { // change regular mana
            if (this.mana > 0) {
                // if not ignore max, do not erase the extra mana from coin-type effects
                // e.g. if we're above max already, adding more mana would change mana by 0
                int manaChange = this.currentIgnoreMax ? this.mana : Math.max(0, Math.min(this.p.maxmana - this.p.mana, this.mana));
                this.p.mana += manaChange;
            } else if (this.mana < 0) {
                this.p.mana = Math.max(0, this.p.mana + this.mana);
            }
        }
    }

    @Override
    public void undo(Board b) {
        this.p.mana = this.prevMana;
        this.p.maxmana = this.prevMaxMana;
    }

    @Override
    public String toString() {
        return this.id + " " + this.p.team + " " + this.mana + " " + this.changeCurrent + " " + this.changeMax + " " + this.currentIgnoreMax + Game.EVENT_END;
    }

    public static EventManaChange fromString(Board b, StringTokenizer st) {
        int team = Integer.parseInt(st.nextToken());
        Player p = b.getPlayer(team);
        int mana = Integer.parseInt(st.nextToken());
        boolean changeCurrent = Boolean.parseBoolean(st.nextToken());
        boolean changeMax = Boolean.parseBoolean(st.nextToken());
        boolean currentIgnoreMax = Boolean.parseBoolean(st.nextToken());
        return new EventManaChange(p, mana, changeCurrent, changeMax, currentIgnoreMax);
    }

    @Override
    public boolean conditions() {
        return this.changeCurrent || this.changeMax;
    }
}
