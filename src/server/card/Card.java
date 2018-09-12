package server.card;

import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import server.Board;
import server.Player;
import server.card.effect.Effect;
import server.card.effect.EffectIDLinker;
import server.card.effect.EffectStats;
import server.event.*;

public class Card {
	public static final Vector2f CARD_DIMENSIONS = new Vector2f(150, 180);
	public static final double EPSILON = 0.0001;
	public static final double NAME_FONT_SIZE = 30;
	public static final double STAT_DEFAULT_SIZE = 30;
	public Board board;
	public boolean alive = true;
	public int id, cardpos, team;
	public String name, text, imagepath;
	public Vector2f targetpos, pos;
	public double scale;
	double speed;
	Image image;
	public CardStatus status;
	public Card realCard; // for visual board

	public Effect finalStatEffects = new Effect(0, ""), finalBasicStatEffects = new Effect(0, "");
	// basic effects cannot be muted
	private LinkedList<Effect> effects = new LinkedList<Effect>(), basicEffects = new LinkedList<Effect>();

	public Card(Board board, CardStatus status, String name, String text, String imagepath, int team, int id) {
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
			// TODO: magic number below is space to display mana cost
			if (font.getWidth(this.name) > (CARD_DIMENSIONS.x - 20) * this.scale) {
				font = Game.getFont("Verdana", (NAME_FONT_SIZE * this.scale * (CARD_DIMENSIONS.x - 20) * this.scale
						/ font.getWidth(this.name)), true, false);
			}
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
		if (this.realCard.board.getPlayer(this.team).canPlayCard(this.realCard)
				&& this.team == this.board.currentplayerturn) {
			g.setColor(org.newdawn.slick.Color.cyan);
			g.drawRect((float) (this.pos.x - CARD_DIMENSIONS.x * this.scale / 2),
					(float) (this.pos.y - CARD_DIMENSIONS.y * this.scale / 2), (float) (CARD_DIMENSIONS.x * this.scale),
					(float) (CARD_DIMENSIONS.y * this.scale));
			g.setColor(org.newdawn.slick.Color.white);
		}
		this.drawStatNumber(g, this.finalStatEffects.getStat(EffectStats.COST),
				this.finalBasicStatEffects.getStat(EffectStats.COST), false, new Vector2f(-0.5f, -0.5f),
				new Vector2f(0.5f, 0.5f), STAT_DEFAULT_SIZE);
	}

	public void drawStatNumber(Graphics g, int stat, int basestat, boolean damaged, Vector2f relpos,
			Vector2f textoffset, double fontsize) {
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
		UnicodeFont font = Game.getFont("Verdana", fontsize * this.scale, true, false, c, Color.BLACK);
		font.drawString(
				this.pos.x + CARD_DIMENSIONS.x * relpos.x * (float) this.scale
						+ font.getWidth("" + stat) * (textoffset.x - 0.5f),
				this.pos.y + CARD_DIMENSIONS.y * relpos.y * (float) this.scale
						+ font.getHeight("" + stat) * (textoffset.y - 0.5f),
				"" + stat);
	}

	public boolean isInside(Vector2f p) {
		return p.x >= this.pos.x - this.image.getWidth() / 2 * this.scale
				&& p.y >= this.pos.y - this.image.getHeight() / 2 * this.scale
				&& p.x <= this.pos.x + this.image.getWidth() / 2 * this.scale
				&& p.y <= this.pos.y + this.image.getHeight() / 2 * this.scale;
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
		e.basic = true;
		e.pos = this.basicEffects.size();
		e.owner = this;
		this.basicEffects.add(e);
		this.updateBasicEffectPositions();
		this.updateBasicEffectStats();
		this.updateFinalEffectStats();
	}

	public void addEffect(Effect e) {
		e.basic = false;
		e.pos = this.effects.size();
		e.owner = this;
		this.effects.add(e);
		this.updateAdditionalEffectPositions();
		this.updateFinalEffectStats();
	}

	public void removeEffect(Effect e) {
		if (this.effects.contains(e)) {
			this.effects.remove(e);
			this.updateAdditionalEffectPositions();
			this.updateFinalEffectStats();
		}
	}

	public void removeAdditionalEffects() {
		while (!this.effects.isEmpty()) {
			this.removeEffect(this.effects.get(0));
		}
	}

	private void updateBasicEffectStats() {
		this.finalBasicStatEffects = new Effect(0, "");
		for (Effect e : this.getBasicEffects()) {
			this.finalBasicStatEffects.applyEffectStats(e);
		}
	}

	private void updateFinalEffectStats() {
		this.finalStatEffects = new Effect(0, "");
		for (Effect e : this.getFinalEffects()) {
			this.finalStatEffects.applyEffectStats(e);
		}
	}

	public void updateBasicEffectPositions() {
		for (int i = 0; i < this.basicEffects.size(); i++) {
			this.basicEffects.get(i).pos = i;
		}
	}

	public void updateAdditionalEffectPositions() {
		for (int i = 0; i < this.effects.size(); i++) {
			this.effects.get(i).pos = i;
		}
	}

	public LinkedList<EventBattlecry> battlecry() {
		LinkedList<EventBattlecry> list = new LinkedList<EventBattlecry>();
		for (Effect e : this.getFinalEffects()) {
			EventBattlecry temp = e.battlecry();
			if (temp != null) {
				list.add(temp);
			}
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
		String ret = list.size() + " ";
		for (Target t : list) {
			ret += t.toString();
		}
		return ret;

	}

	public void battlecryTargetsFromString(Board b, StringTokenizer st) {
		int num = Integer.parseInt(st.nextToken());
		for (int i = 0; i < num; i++) {
			this.getBattlecryTargets().get(i).copyFromString(b, st);
		}
	}

	public LinkedList<EventFlag> onEvent(Event event) {
		LinkedList<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onEvent(event);
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public String cardPosToString() {
		return this.status.toString() + " " + this.cardpos + " ";
	}

	public String toString() {
		return "card " + this.id + " " + this.cardPosToString();
	}

	public String toConstructorString() {
		return this.id + " " + this.team + " ";
	}

	public static Card createFromConstructorString(Board b, StringTokenizer st) {
		int id = Integer.parseInt(st.nextToken());
		int team = Integer.parseInt(st.nextToken());
		Class<? extends Card> c = CardIDLinker.getClass(id);
		Card card = null;
		try {
			card = (Card) c.getConstructor(Board.class, int.class).newInstance(b, team);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return card;
		// AAAAAAAAAAAAAAAAAAAa
	}

	public String toReference() {
		return this.team + " " + this.cardPosToString();
	}

	public static Card fromReference(Board b, StringTokenizer reference) {
		String firsttoken = reference.nextToken();
		if (firsttoken.equals("null")) {
			return null;
		}
		int team = Integer.parseInt(firsttoken);
		Player p = b.getPlayer(team);
		String status = reference.nextToken();
		int cardpos = Integer.parseInt(reference.nextToken());
		if (cardpos == -1) { // mission failed we'll get em next time
			return null;
		}
		switch (status) {
		case "HAND":
			return p.hand.cards.get(cardpos);
		case "BOARD":
			return b.getBoardObject(team, cardpos);
		case "DECK":
			return p.deck.cards.get(cardpos);
		case "GRAVEYARD":
			return p.board.getGraveyard(team).get(cardpos);
		case "UNLEASHPOWER":
			return p.unleashPower;
		default:
			return null;
		}

	}
}
