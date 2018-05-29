package server.event;

import java.util.LinkedList;

import server.Player;

public class EventManaChange extends Event {
	Player p;
	int mana;
	boolean empty, recover;

	public EventManaChange(Player p, int mana, boolean empty, boolean recover) {
		this.p = p;
		this.mana = mana;
		this.empty = empty;
		this.recover = recover;
	}

	public String resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		if (!this.conditions()) {
			return this.toString();
		}
		if (!this.recover) {
			if (this.p.maxmana + this.mana > this.p.maxmaxmana) {
				this.p.maxmana = this.p.maxmaxmana;
			} else if (this.p.maxmana + this.mana < 0) {
				this.p.maxmana = 0;
			} else {
				this.p.maxmana += this.mana;
			}
		}
		if (!this.empty) {
			if (this.p.mana + this.mana > this.p.maxmana) {
				this.p.mana = this.p.maxmana;
			} else if (this.p.mana + this.mana < 0) {
				this.p.mana = 0;
			} else {
				this.p.mana += this.mana;
			}
		}

		return this.toString();
	}

	public String toString() {
		return "manac " + this.p.toString() + " " + this.mana + " " + this.empty + " " + this.recover;
	}

	public boolean conditions() {
		return !(this.empty && this.recover);
	}
}
