package server.playeraction;

import java.util.*;

import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

public class UnleashMinionAction extends PlayerAction {

	public static final int ID = 2;

	public Player p;
	public Minion m;

	public UnleashMinionAction(Player p, Minion m) {
		super(ID);
		// TODO Auto-generated constructor stub
		this.p = p;
		this.m = m;
	}

	// remember to set targets to unleash upon
	@Override
	public boolean perform(Board b) {
		if (!p.canUnleashCard(m)) {
			return false;
		}
		b.eventlist
				.add(new EventManaChange(p, -p.unleashPower.finalStatEffects.getStat(EffectStats.COST), false, true));
		b.eventlist.addAll(p.unleashPower.unleash(m));
		b.resolveAll();
		return true;
	}

	@Override
	public String toString() {
		return this.ID + " " + p.team + " " + m.toReference() + " " + m.unleashTargetsToString() + "\n";
	}

	public static UnleashMinionAction fromString(Board b, StringTokenizer st) {
		Player p = b.getPlayer(Integer.parseInt(st.nextToken()));
		Minion m = (Minion) Card.fromReference(b, st);
		m.unleashTargetsFromString(b, st);
		return new UnleashMinionAction(p, m);
	}

}
