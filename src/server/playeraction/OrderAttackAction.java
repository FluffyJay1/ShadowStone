package server.playeraction;

import server.card.Card;
import server.card.Minion;
import server.event.EventMinionAttack;

import java.util.StringTokenizer;

import server.Board;

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

	public void perform(Board b) {
		if (attacker.getAttackableTargets().contains(victim)) {
			b.eventlist.add(new EventMinionAttack(attacker, victim));
			b.resolveAll();
		}
	}

	public String toString() {
		return this.ID + " " + attacker.toReference() + " " + victim.toReference() + "\n";
	}

	public static OrderAttackAction fromString(Board b, StringTokenizer st) {
		Minion attacker = (Minion) Card.fromReference(b, st);
		Minion victim = (Minion) Card.fromReference(b, st);
		return new OrderAttackAction(attacker, victim);
	}
}
