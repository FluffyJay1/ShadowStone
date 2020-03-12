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
	String unleashTargets;

	public UnleashMinionAction(Player p, Minion m, String unleashTargets) {
		super(ID);
		// TODO Auto-generated constructor stub
		this.p = p;
		this.m = m;
		this.unleashTargets = unleashTargets;
	}

	// remember to set targets to unleash upon
	@Override
	public List<Event> perform(Board b) {
		if (!p.canUnleashCard(m)) {
			return new LinkedList<Event>();
		}
		Target.setListFromString(m.getUnleashTargets(), b, new StringTokenizer(this.unleashTargets));
		b.eventlist
				.add(new EventManaChange(p, -p.unleashPower.finalStatEffects.getStat(EffectStats.COST), false, true));
		b.eventlist.addAll(p.unleashPower.unleash(m));
		return b.resolveAll();
	}

	@Override
	public String toString() {
		return this.ID + " " + p.team + " " + m.toReference() + this.unleashTargets + "\n";
	}

	public static UnleashMinionAction fromString(Board b, StringTokenizer st) {
		Player p = b.getPlayer(Integer.parseInt(st.nextToken()));
		Minion m = (Minion) Card.fromReference(b, st);
		Target.setListFromString(m.getUnleashTargets(), b, st);
		return new UnleashMinionAction(p, m, Target.listToString(m.getUnleashTargets()));
	}

}
