package server.card;

import java.awt.Font;
import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import server.Board;
import server.event.Event;
import server.event.EventDamage;
import server.event.EventDraw;

public class Minion extends BoardObject {
	public static final double STAT_DEFAULT_SIZE = 24;
	public Stats stats, baseStats;
	public int maxHealth;

	public Minion(Board board, CardStatus status, int cost, int attack, int magic, int health, String name, String text,
			String imagepath, int team, int id) {
		super(board, status, cost, name, text, imagepath, team, id);
		this.stats = new Stats(attack, magic, health);
		this.baseStats = new Stats(attack, magic, health);
		this.maxHealth = health;
	}

	@Override
	public void drawOnBoard(Graphics g) {
		this.drawStatNumber(g, this.stats.a, new Vector2f(-0.4f, 0.5f), new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.stats.m, new Vector2f(0, 0.5f), new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.stats.h, new Vector2f(0.4f, 0.5f), new Vector2f(0, -0.5f));
	}

	@Override
	public void drawInHand(Graphics g) {
		this.drawStatNumber(g, this.stats.a, new Vector2f(-0.4f, 0f), new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.stats.m, new Vector2f(-0.4f, 0.25f), new Vector2f(0, -0.5f));
		this.drawStatNumber(g, this.stats.h, new Vector2f(-0.4f, 0.5f), new Vector2f(0, -0.5f));
	}

	private void drawStatNumber(Graphics g, int stat, Vector2f relpos, Vector2f textoffset) {
		UnicodeFont font = Game.getFont("Verdana", STAT_DEFAULT_SIZE * this.scale, true, false);
		font.drawString(
				this.pos.x + CARD_DIMENSIONS.x * relpos.x * (float) this.scale
						+ font.getWidth("" + stat) * (textoffset.x - 0.5f),
				this.pos.y + CARD_DIMENSIONS.y * relpos.y * (float) this.scale
						+ font.getHeight("" + stat) * (textoffset.y - 0.5f),
				"" + stat);
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

	public LinkedList<Event> onDamaged(int damage) {
		return new LinkedList<Event>();
	}

	public String toString() {
		return "Minion " + name + " cost " + cost + " position " + boardpos + " alive " + alive + "\n"
				+ this.stats.toString();
	}
}
