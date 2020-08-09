package server.event;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;

public class EventTurnStart extends Event {
	public static final int ID = 15;
	public Player p;
	private int prevCurrentPlayerTurn;
	private int prevUnleashesThisTurn;
	private List<Boolean> prevSickness;
	private List<Integer> prevAttacks;

	public EventTurnStart(Player p) {
		super(ID, false);
		this.p = p;
	}

	@Override
	public void resolve(List<Event> eventlist, boolean loopprotection) {
		this.prevCurrentPlayerTurn = this.p.board.currentPlayerTurn;
		this.prevUnleashesThisTurn = this.p.unleashPower.unleashesThisTurn;
		this.prevSickness = new ArrayList<Boolean>();
		this.prevAttacks = new ArrayList<Integer>();
		this.p.board.currentPlayerTurn = this.p.team;
		this.p.unleashPower.unleashesThisTurn = 0;
		eventlist.add(new EventDraw(this.p));
		eventlist.add(new EventManaChange(this.p, 1, true, false));
		eventlist.add(new EventManaChange(this.p, this.p.maxmana + 1, false, true));
		Minion leader = this.p.leader;
		eventlist.addAll(leader.onTurnStart());
		for (BoardObject b : this.p.board.getBoardObjects(this.p.team)) {
			eventlist.addAll(b.onTurnStart());
			if (b instanceof Minion) {
				this.prevSickness.add(((Minion) b).summoningSickness);
				this.prevAttacks.add(((Minion) b).attacksThisTurn);
				((Minion) b).summoningSickness = false;
				((Minion) b).attacksThisTurn = 0;
			} else {
				this.prevSickness.add(false);
				this.prevAttacks.add(0);
			}
			if (b.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
				EffectStatChange e = new EffectStatChange("");
				e.change.setStat(EffectStats.COUNTDOWN, -1);
				eventlist.add(new EventAddEffect(b, e));
			}
		}

	}

	@Override
	public void undo() {
		this.p.board.currentPlayerTurn = this.prevCurrentPlayerTurn;
		this.p.unleashPower.unleashesThisTurn = this.prevUnleashesThisTurn;
		List<BoardObject> boardObjects = this.p.board.getBoardObjects(this.p.team);
		for (int i = 0; i < boardObjects.size(); i++) {
			BoardObject bo = boardObjects.get(i);
			if (bo instanceof Minion) {
				Minion m = (Minion) bo;
				m.summoningSickness = this.prevSickness.get(i);
				m.attacksThisTurn = this.prevAttacks.get(i);
			}
		}
	}

	@Override
	public String toString() {
		return this.id + " " + this.p.team + "\n";
	}

	public static EventTurnStart fromString(Board b, StringTokenizer st) {
		int team = Integer.parseInt(st.nextToken());
		Player p = b.getPlayer(team);
		return new EventTurnStart(p);
	}

	@Override
	public boolean conditions() {
		return true;
	}
}
