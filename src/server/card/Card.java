package server.card;

import java.awt.Color;
import java.lang.reflect.*;
import java.util.*;

import org.newdawn.slick.*;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.*;

import client.Game;
import client.tooltip.*;
import server.*;
import server.card.cardpack.*;
import server.card.effect.*;
import server.event.*;
import server.event.Event;

public class Card {
	public static final Vector2f CARD_DIMENSIONS = new Vector2f(150, 180);
	public static final double EPSILON = 0.0001;
	public static final double NAME_FONT_SIZE = 30;
	public static final double STAT_DEFAULT_SIZE = 30;
	public Board board;
	public boolean alive = true;
	public int id, cardpos, team;
	public TooltipCard tooltip;
	public String imagepath;
	public Vector2f targetpos, pos;
	public double scale;
	double speed;
	Image image;
	public CardStatus status;
	public ClassCraft craft;
	public Card realCard; // for visual board

	public Effect finalStatEffects = new Effect(0, ""), finalBasicStatEffects = new Effect(0, "");
	// basic effects don't get removed when removed from board (e.g. bounce
	// effects)
	private LinkedList<Effect> effects = new LinkedList<Effect>(), basicEffects = new LinkedList<Effect>();

	public Card(Board board, TooltipCard tooltip) {
		this.board = board;
		this.tooltip = tooltip;
		this.imagepath = tooltip.imagepath;
		this.targetpos = new Vector2f();
		this.pos = new Vector2f();
		this.speed = 0.999;
		this.scale = 1;
		this.id = tooltip.id;
		this.status = CardStatus.DECK;
		this.craft = tooltip.craft;
	}

	public void update(double frametime) {
		Vector2f delta = this.targetpos.copy().sub(this.pos);
		if (delta.length() > EPSILON) {
			float ratio = 1 - (float) Math.pow(1 - this.speed, frametime);
			this.pos.add(delta.scale(ratio));
		}
	}

	public void draw(Graphics g) {
		if (this.image == null && this.imagepath != null) {
			this.image = Game.getImage(tooltip.imagepath).getScaledCopy((int) CARD_DIMENSIONS.x,
					(int) CARD_DIMENSIONS.y);
		}
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
			if (font.getWidth(this.tooltip.name) > (CARD_DIMENSIONS.x - 20) * this.scale) {
				font = Game.getFont("Verdana", (NAME_FONT_SIZE * this.scale * (CARD_DIMENSIONS.x - 20) * this.scale
						/ font.getWidth(this.tooltip.name)), true, false);
			}
			font.drawString(this.pos.x - font.getWidth(this.tooltip.name) / 2,
					this.pos.y - CARD_DIMENSIONS.y * (float) this.scale / 2, this.tooltip.name);
			this.drawInHand(g);
			break;
		default:
			break;
		}
	}

	public void drawOnBoard(Graphics g) {

	}

	public void drawInHand(Graphics g) {
		if (this.realCard != null && this.realCard.board.getPlayer(this.team).canPlayCard(this.realCard)
				&& this.board.getPlayer(this.team).canPlayCard(this)) {
			g.setColor(org.newdawn.slick.Color.cyan);
			g.drawRect((float) (this.pos.x - CARD_DIMENSIONS.x * this.scale / 2),
					(float) (this.pos.y - CARD_DIMENSIONS.y * this.scale / 2), (float) (CARD_DIMENSIONS.x * this.scale),
					(float) (CARD_DIMENSIONS.y * this.scale));
			g.setColor(org.newdawn.slick.Color.white);
		}
		this.drawCostStat(g, this.finalStatEffects.getStat(EffectStats.COST),
				this.finalBasicStatEffects.getStat(EffectStats.COST), new Vector2f(-0.5f, -0.5f),
				new Vector2f(0.5f, 0.5f), STAT_DEFAULT_SIZE);
	}

	public void drawStatNumber(Graphics g, int stat, Vector2f relpos, Vector2f textoffset, double fontsize, Color c) {
		UnicodeFont font = Game.getFont("Verdana", fontsize * this.scale, true, false, c, Color.BLACK);
		font.drawString(
				this.pos.x + CARD_DIMENSIONS.x * relpos.x * (float) this.scale
						+ font.getWidth("" + stat) * (textoffset.x - 0.5f),
				this.pos.y + CARD_DIMENSIONS.y * relpos.y * (float) this.scale
						+ font.getHeight("" + stat) * (textoffset.y - 0.5f),
				"" + stat);
	}

	public void drawCostStat(Graphics g, int cost, int basecost, Vector2f relpos, Vector2f textoffset,
			double fontsize) {
		Color c = Color.white;
		if (cost > basecost) {
			c = Color.red;
		}
		if (cost < basecost) {
			c = Color.green;
		}
		this.drawStatNumber(g, cost, relpos, textoffset, fontsize, c);
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

	public void muteEffect(Effect e, boolean mute) {
		if (this.effects.contains(e)) {
			e.mute = mute;
			this.updateBasicEffectStats();
		}
	}

	public void updateBasicEffectStats() {
		this.finalBasicStatEffects = new Effect(0, "");
		for (Effect e : this.getBasicEffects()) {
			this.finalBasicStatEffects.applyEffectStats(e);
		}
		for (int i = 0; i < EffectStats.NUM_STATS; i++) {
			if (this.finalBasicStatEffects.getStat(i) < 0) {
				this.finalBasicStatEffects.set.setStat(i, 0);
			}
		}
		this.updateFinalEffectStats();
	}

	public void updateFinalEffectStats() {
		this.finalStatEffects = new Effect(0, "");
		for (Effect e : this.getFinalEffects()) {
			this.finalStatEffects.applyEffectStats(e);
		}
		for (int i = 0; i < EffectStats.NUM_STATS; i++) {
			if (this.finalStatEffects.getStat(i) < 0) {
				this.finalStatEffects.set.setStat(i, 0);
			}
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

	@Override
	public String toString() {
		return "card " + this.id + " " + this.cardPosToString();
	}

	public String toConstructorString() {
		return this.id + " ";
	}

	public static Card createFromConstructorString(Board b, StringTokenizer st) {
		int id = Integer.parseInt(st.nextToken());
		return createFromConstructor(b, id);

	}

	public static Card createFromConstructor(Board b, int id) {
		Class<? extends Card> c = CardSet.getCardClass(id);
		Card card = null;
		try {
			card = c.getConstructor(Board.class).newInstance(b);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return card;
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
