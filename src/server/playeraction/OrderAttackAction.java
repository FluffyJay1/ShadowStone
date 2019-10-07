package server.playeraction;

import java.util.*;

import server.*;
import server.card.*;
import server.event.*;

public class OrderAttackAction extends PlayerAction {

	public static final int ID = 3;

	public Minion attacker;
	public Minion victim;

	public OrderAttackAction(Minion attacker, Minion victim) {
		super(ID);
		// TODO Auto-generated constructor stub
		this.attacker = attacker;
		this.victim = victim;
	}

	@Override
	public List<Event> perform(Board b) {
		if (this.attacker.getAttackableTargets().contains(this.victim) && this.attacker.canAttack()) {
			b.eventlist.add(new EventMinionAttack(this.attacker, this.victim));
			return b.resolveAll();
		}
		return new LinkedList<Event>();
	}

	@Override
	public String toString() {
		return this.ID + " " + this.attacker.toReference() + this.victim.toReference() + "\n";
	}

	public static OrderAttackAction fromString(Board b, StringTokenizer st) {
		Minion attacker = (Minion) Card.fromReference(b, st);
		Minion victim = (Minion) Card.fromReference(b, st);
		return new OrderAttackAction(attacker, victim);
	}
}
