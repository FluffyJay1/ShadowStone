package server.playeraction;

import java.util.StringTokenizer;

import server.Player;
import server.card.Card;
import server.card.Minion;
import server.card.effect.EffectStats;
import server.event.EventManaChange;
import server.Board;

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
	public void perform(Board b) {
		if (!p.canUnleashCard(m)) {
			return;
		}
		b.eventlist
				.add(new EventManaChange(p, -p.unleashPower.finalStatEffects.getStat(EffectStats.COST), false, true));
		b.eventlist.addAll(p.unleashPower.unleash(m));
		b.resolveAll();
	}

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
