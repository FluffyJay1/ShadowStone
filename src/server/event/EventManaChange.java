package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;

public class EventManaChange extends Event {
	public static final int ID = 6;
	Player p;
	int mana;
	boolean empty, recover;

	public EventManaChange(Player p, int mana, boolean empty, boolean recover) {
		super(ID);
		this.p = p;
		this.mana = mana;
		this.empty = empty;
		this.recover = recover;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
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
	}

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

	public boolean conditions() {
		return !(this.empty && this.recover);
	}
}
