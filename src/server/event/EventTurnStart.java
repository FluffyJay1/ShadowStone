package server.event;

import java.util.LinkedList;
import java.util.StringTokenizer;

import server.Board;
import server.Player;
import server.card.BoardObject;
import server.card.Minion;
import server.card.effect.EffectStatChange;
import server.card.effect.EffectStats;

public class EventTurnStart extends Event {
	public static final int ID = 15;
	public Player p;

	public EventTurnStart(Player p) {
		super(ID);
		this.p = p;
	}

	public void resolve(LinkedList<Event> eventlist, boolean loopprotection) {
		this.p.board.currentplayerturn = this.p.team;
		this.p.unleashPower.unleashesThisTurn = 0;
		eventlist.add(new EventDraw(this.p, 1));
		eventlist.add(new EventManaChange(this.p, 1, true, false));
		eventlist.add(new EventManaChange(this.p, this.p.maxmana + 1, false, true));
		Minion leader = (Minion) this.p.board.getBoardObject(this.p.team, 0);
		eventlist.addAll(leader.onTurnStart());
		for (BoardObject b : this.p.board.getBoardObjects(this.p.team)) {
			eventlist.addAll(b.onTurnStart());
			if (b instanceof Minion) {
				((Minion) b).summoningSickness = false;
				((Minion) b).attacksThisTurn = 0;
			}
			if (b.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
				EffectStatChange e = new EffectStatChange("");
				e.change.setStat(EffectStats.COUNTDOWN, -1);
				eventlist.add(new EventAddEffect(b, e));
			}
		}

	}

	public String toString() {
		return this.id + " " + this.p.team + "\n";
	}

	public static EventTurnStart fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		return new EventTurnStart(p);
	}

	public boolean conditions() {
		return true;
	}
}
