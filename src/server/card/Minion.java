package server.card;

import java.util.LinkedList;

import server.Board;
import server.event.Event;
import server.event.EventDamage;

public class Minion extends BoardObject {
	public int attack, magic, health, maxhealth;

	public Minion(Board board, int cost, int attack, int magic, int health, String name, String text, String imagepath,
			int id) {
		super(board, cost, name, text, imagepath, id);
		this.attack = attack;
		this.magic = magic;
		this.health = health;
		this.maxhealth = health;
	}

	public LinkedList<Event> onAttack(Minion target) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> onAttacked(Minion target) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> clash(Minion target) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> takeDamage(int damage) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventDamage(this, damage));
		return list;
	}

	public static String statsToString(int attack, int magic, int health) {
		return "(A: " + attack + ", M: " + magic + ", H: " + health + ")";
	}

	public String toString() {
		return "Minion " + name + " cost " + cost + " position " + position + " alive " + alive + "\n"
				+ Minion.statsToString(attack, magic, health);
	}
}