package server.card;

import java.awt.Font;
import java.util.LinkedList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.TrueTypeFont;

import client.Game;
import server.Board;
import server.event.Event;
import server.event.EventDamage;
import server.event.EventDraw;

public class Minion extends BoardObject {
	public static final double STAT_DEFAULT_SIZE = 24;
	public int attack, magic, health, maxhealth;

	public Minion(Board board, CardStatus status, int cost, int attack, int magic, int health, String name, String text,
			String imagepath, int id) {
		super(board, status, cost, name, text, imagepath, id);
		this.attack = attack;
		this.magic = magic;
		this.health = health;
		this.maxhealth = health;
	}

	@Override
	public void drawOnBoard(Graphics g) {
		TrueTypeFont font = Game.getFont("Verdana", Font.BOLD, (int) (STAT_DEFAULT_SIZE * this.scale));
		// attack
		font.drawString(this.pos.x - CARD_DIMENSIONS.x * (float) this.scale / 2 - font.getWidth("" + this.attack) / 2,
				this.pos.y + CARD_DIMENSIONS.y * (float) this.scale / 2 - (float) STAT_DEFAULT_SIZE, "" + this.attack);
		// magic
		font.drawString(this.pos.x - font.getWidth("" + this.magic) / 2,
				this.pos.y + CARD_DIMENSIONS.y * (float) this.scale / 2 - (float) STAT_DEFAULT_SIZE, "" + this.magic);
		// health
		font.drawString(this.pos.x + CARD_DIMENSIONS.x * (float) this.scale / 2 - font.getWidth("" + this.health) / 2,
				this.pos.y + CARD_DIMENSIONS.y * (float) this.scale / 2 - (float) STAT_DEFAULT_SIZE, "" + this.health);
	}

	@Override
	public void drawInHand(Graphics g) {
		TrueTypeFont font = Game.getFont("Verdana", Font.BOLD, (int) (STAT_DEFAULT_SIZE * this.scale));
		// attack
		font.drawString(this.pos.x - CARD_DIMENSIONS.x * (float) this.scale / 2 - font.getWidth("" + this.attack) / 2,
				this.pos.y + CARD_DIMENSIONS.y * (float) this.scale / 2 - (float) STAT_DEFAULT_SIZE, "" + this.attack);
		// magic
		font.drawString(this.pos.x - font.getWidth("" + this.magic) / 2,
				this.pos.y + CARD_DIMENSIONS.y * (float) this.scale / 2 - (float) STAT_DEFAULT_SIZE, "" + this.magic);
		// health
		font.drawString(this.pos.x + CARD_DIMENSIONS.x * (float) this.scale / 2 - font.getWidth("" + this.health) / 2,
				this.pos.y + CARD_DIMENSIONS.y * (float) this.scale / 2 - (float) STAT_DEFAULT_SIZE, "" + this.health);
	}

	public LinkedList<Event> onAttack(Minion target) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> onAttacked(Minion target) {
		return new LinkedList<Event>();
	}

	public LinkedList<Event> clash(Minion target) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventDraw(this.board.getPlayer(this.team), 1));
		return list;
		// return new LinkedList<Event>();
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
