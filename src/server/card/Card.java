package server.card;

import java.awt.Color;
import java.awt.Font;
import java.util.LinkedList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import server.Board;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.Event;

public class Card {
	public static final Vector2f CARD_DIMENSIONS = new Vector2f(150, 200);
	public static final double EPSILON = 0.0001;
	public static final double NAME_FONT_SIZE = 24;
	public static final double STAT_DEFAULT_SIZE = 24;
	public Board board;
	public boolean alive = true;
	public int id, handpos, team;
	public String name, text, imagepath;
	public Vector2f targetpos, pos;
	public double scale;
	double speed;
	Image image;
	public CardStatus status;

	public Effect finalStatEffects = new Effect(this, 0, ""), finalBasicStatEffects = new Effect(this, 0, "");
	// basic effects cannot be muted
	private LinkedList<Effect> effects = new LinkedList<Effect>(), basicEffects = new LinkedList<Effect>();

	public Card() {
		this.status = CardStatus.BOARD;
		this.team = 0;
	}

	public Card(Board board, CardStatus status, int cost, String name, String text, String imagepath, int team,
			int id) {
		this.board = board;
		this.name = name;
		this.text = text;
		if (imagepath != null) {
			this.image = Game.getImage(imagepath).getScaledCopy((int) CARD_DIMENSIONS.x, (int) CARD_DIMENSIONS.y);
		}
		this.imagepath = imagepath;
		this.targetpos = new Vector2f();
		this.pos = new Vector2f();
		this.speed = 0.999;
		this.scale = 1;
		this.id = id;
		this.status = status;
		this.team = team;
		this.addBasicEffect(new Effect(this, 0, "", cost));
	}

	public void update(double frametime) {
		Vector2f delta = this.targetpos.copy().sub(this.pos);
		if (delta.length() > EPSILON) {
			float ratio = 1 - (float) Math.pow(1 - this.speed, frametime);
			this.pos.add(delta.scale(ratio));
		}
	}

	public void draw(Graphics g) {
		Image scaledCopy = this.image.getScaledCopy((float) this.scale);
		g.drawImage(scaledCopy, (int) (this.pos.x - CARD_DIMENSIONS.x * this.scale / 2),
				(int) (this.pos.y - CARD_DIMENSIONS.y * this.scale / 2));
		switch (this.status) {
		case BOARD:
			this.drawOnBoard(g);
			break;
		case HAND:
			UnicodeFont font = Game.getFont("Verdana", (NAME_FONT_SIZE * this.scale), true, false);
			font.drawString(this.pos.x - font.getWidth(this.name) / 2,
					this.pos.y - CARD_DIMENSIONS.y * (float) this.scale / 2, this.name);
			this.drawInHand(g);
			break;
		default:
			break;
		}
	}

	public void drawOnBoard(Graphics g) {

	}

	public void drawInHand(Graphics g) {
		if (this.board.getPlayer(this.team).canPlayCard(this)) {
			g.setColor(org.newdawn.slick.Color.cyan);
			g.drawRect((float) (this.pos.x - CARD_DIMENSIONS.x * this.scale / 2),
					(float) (this.pos.y - CARD_DIMENSIONS.y * this.scale / 2), (float) (CARD_DIMENSIONS.x * this.scale),
					(float) (CARD_DIMENSIONS.y * this.scale));
			g.setColor(org.newdawn.slick.Color.white);
		}
		this.drawStatNumber(g, this.finalStatEffects.getStat(EffectStats.COST),
				this.finalBasicStatEffects.getStat(EffectStats.COST), false, new Vector2f(-0.5f, -0.5f),
				new Vector2f(0.5f, 0.5f));
	}

	public void drawStatNumber(Graphics g, int stat, int basestat, boolean damaged, Vector2f relpos,
			Vector2f textoffset) {
		Color c = Color.white;
		if (damaged) {
			c = Color.red;
		} else {
			if (stat > basestat) {
				c = Color.green;
			}
			if (stat < basestat) {
				c = Color.orange;
			}
		}
		UnicodeFont font = Game.getFont("Verdana", STAT_DEFAULT_SIZE * this.scale, true, false, c, Color.BLACK);
		font.drawString(
				this.pos.x + CARD_DIMENSIONS.x * relpos.x * (float) this.scale
						+ font.getWidth("" + stat) * (textoffset.x - 0.5f),
				this.pos.y + CARD_DIMENSIONS.y * relpos.y * (float) this.scale
						+ font.getHeight("" + stat) * (textoffset.y - 0.5f),
				"" + stat);
	}

	public LinkedList<Effect> getBasicEffects() {
		return this.basicEffects;
	}

	public LinkedList<Effect> getAdditionalEffects() {
		return this.effects;
	}

	public LinkedList<Effect> getFinalEffects() {
		LinkedList<Effect> list = new LinkedList<Effect>();
		list.addAll(this.getBasicEffects());
		for (Effect e : this.getAdditionalEffects()) {
			if (!e.mute) {
				list.add(e);
			}
		}
		return list;
	}

	public void addBasicEffect(Effect e) {
		this.basicEffects.add(e);
		this.updateBasicEffectStats();
		this.updateFinalEffectStats();
	}

	public void addEffect(Effect e) {
		this.effects.add(e);
		this.updateFinalEffectStats();
	}

	public void updateBasicEffectStats() {
		this.finalBasicStatEffects = new Effect(this, 0, "");
		for (Effect e : this.getBasicEffects()) {
			this.finalBasicStatEffects.applyEffectStats(e);
		}
	}

	public void updateFinalEffectStats() {
		this.finalStatEffects = new Effect(this, 0, "");
		for (Effect e : this.getFinalEffects()) {
			this.finalStatEffects.applyEffectStats(e);
		}
	}

	public LinkedList<Event> battlecry() {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.battlecry());
		}
		return list;
	}

	public boolean conditions() { // to be able to even begin to play
		return true;
	}

	public LinkedList<Target> getBattlecryTargets() {
		LinkedList<Target> list = new LinkedList<Target>();
		for (Effect e : this.getFinalEffects()) {
			for (Target t : e.battlecryTargets) {
				list.add(t);
			}
		}
		return list;
	}

	public void resetBattlecryTargets() {
		for (Target t : this.getBattlecryTargets()) {
			t.reset();
		}
	}

	public Target getNextNeededBattlecryTarget() {
		for (Target t : this.getBattlecryTargets()) {
			if (!t.ready()) {
				return t;
			}
		}
		return null;
	}

	public String battlecryTargetsToString() {
		LinkedList<Target> list = this.getBattlecryTargets();
		String ret = "btargets " + list.size() + " ";
		for (Target t : list) {
			ret += t.toString() + " ";
		}
		return ret;

	}

	public boolean isInside(Vector2f p) {
		return p.x >= this.pos.x - this.image.getWidth() / 2 * this.scale
				&& p.y >= this.pos.y - this.image.getHeight() / 2 * this.scale
				&& p.x <= this.pos.x + this.image.getWidth() / 2 * this.scale
				&& p.y <= this.pos.y + this.image.getHeight() / 2 * this.scale;
	}

	public String posToString() {
		switch (this.status) {
		case HAND:
			return "hand " + this.handpos;
		case BOARD:
			return "board";
		case DECK:
			return "deck";
		default:
			return "";
		}
	}

	public String toString() {
		return "card " + this.id + " " + this.posToString();
	}
}
