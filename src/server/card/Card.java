package server.card;

import java.lang.reflect.*;
import java.util.*;

import client.tooltip.*;
import client.ui.game.*;
import server.*;
import server.card.cardpack.*;
import server.card.effect.*;
import server.event.*;

public class Card implements Cloneable {
	public Board board;
	public boolean alive = true;
	public int id, cardpos, team;
	public TooltipCard tooltip;
	public CardStatus status;
	public ClassCraft craft;
	public Card realCard; // for visual board
	public UICard uiCard;

	public Effect finalStatEffects = new Effect(0, ""), finalBasicStatEffects = new Effect(0, "");
	// basic effects don't get removed when removed from board (e.g. bounce
	// effects)
	private List<Effect> effects = new LinkedList<Effect>(), basicEffects = new LinkedList<Effect>();

	public Card(Board board, TooltipCard tooltip) {
		this.board = board;
		this.tooltip = tooltip;
		this.id = tooltip.id;
		this.status = CardStatus.DECK;
		this.craft = tooltip.craft;
	}

	/**
	 * Estimates a "power level" of a card, for an AI to use to evaluate board
	 * state. Values should be in terms of equivalent mana worth.
	 * 
	 * @return the approximate mana worth of the card
	 */
	public double getValue() {
		return this.finalBasicStatEffects.getStat(EffectStats.COST) + 1;
	}

	public List<Effect> getEffects(boolean basic) {
		return basic ? this.basicEffects : this.effects;
	}

	public List<Effect> getFinalEffects() {
		LinkedList<Effect> list = new LinkedList<Effect>();
		list.addAll(this.getEffects(true));
		for (Effect e : this.getEffects(false)) {
			if (!e.mute) {
				list.add(e);
			}
		}
		return list;
	}

	/**
	 * Adds an effect to the card. If the effect is also flagged as an event
	 * listener, it is registered with the board.
	 * 
	 * @param basic
	 *            Whether the effect is a basic effect of the card
	 * @param pos
	 *            The position to add the effect to
	 * @param e
	 *            The effect
	 */
	public void addEffect(boolean basic, int pos, Effect e) {
		e.basic = basic;
		e.pos = pos;
		e.owner = this;
		this.getEffects(basic).add(pos, e);
		if (this.board != null && e.listener) {
			this.board.registerEventListener(e);
		}
		this.updateEffectPositions(basic);
		this.updateEffectStats(basic);
	}

	public void addEffect(boolean basic, Effect e) {
		this.addEffect(basic, this.getEffects(basic).size(), e);
	}

	public void removeEffect(Effect e) {
		if (this.effects.contains(e)) {
			this.effects.remove(e);
			if (this.board != null && e.listener) {
				this.board.removeEventListener(e);
			}
			this.updateEffectPositions(false);
			this.updateEffectStats(false);
		}
	}

	public List<Effect> removeAdditionalEffects() {
		List<Effect> ret = new LinkedList<Effect>();
		while (!this.effects.isEmpty()) {
			ret.add(this.effects.get(0));
			this.removeEffect(this.effects.get(0));
		}
		return ret;
	}

	public void muteEffect(Effect e, boolean mute) {
		if (this.effects.contains(e)) {
			e.mute = mute;
			this.updateEffectStats(false);
		}
	}

	// updates stat numbers, if a basic effect changed then it tallies the stat
	// numbers for both basic and additional effects, caching the tally for the
	// base stat numbers for future use
	public void updateEffectStats(boolean basic) {
		// start with clean slate
		Effect stats = new Effect(0, "", 1);
		if (basic) {
			this.finalBasicStatEffects = stats;
		} else {
			this.finalStatEffects = stats;
		}
		List<Effect> relevant = basic ? this.getEffects(basic) : this.getFinalEffects();
		for (Effect e : relevant) {
			stats.applyEffectStats(e);
		}
		for (int i = 0; i < EffectStats.NUM_STATS; i++) {
			if (stats.getStat(i) < 0) {
				stats.set.setStat(i, 0);
			}
		}
		if (basic) {
			// update the stat numbers for the additional effects too
			this.updateEffectStats(false);
		}
	}

	public void updateEffectPositions(boolean basic) {
		List<Effect> relevant = this.getEffects(basic);
		for (int i = 0; i < relevant.size(); i++) {
			relevant.get(i).pos = i;
		}
	}

	public List<EventBattlecry> battlecry() {
		List<EventBattlecry> list = new LinkedList<EventBattlecry>();
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

	public List<Target> getBattlecryTargets() {
		List<Target> list = new LinkedList<Target>();
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
		return Target.listToString(this.getBattlecryTargets());
	}

	public void battlecryTargetsFromString(Board b, StringTokenizer st) {
		int num = Integer.parseInt(st.nextToken());
		for (int i = 0; i < num; i++) {
			this.getBattlecryTargets().get(i).copyFromString(b, st);
		}
	}

	// cloning a card ingame should be done as a create card + copy effect
	// events
	@Override
	public Card clone() throws CloneNotSupportedException {
		Card c = (Card) super.clone();
		c.basicEffects = new LinkedList<Effect>();
		for (Effect e : this.basicEffects) {
			c.addEffect(true, e.clone());
		}
		c.effects = new LinkedList<Effect>();
		for (Effect e : this.effects) {
			c.addEffect(false, e.clone());
		}
		return c;
	}

	public String cardPosToString() {
		return this.status.toString() + " " + this.cardpos + " ";
	}

	// TODO make a corresponding fromString method
	@Override
	public String toString() {
		String ret = this.id + " " + this.team + " " + this.cardPosToString() + this.basicEffects.size() + " ";
		for (Effect e : this.basicEffects) {
			ret += e.toString();
		}
		ret += this.effects.size() + " ";
		for (Effect e : this.effects) {
			ret += e.toString();
		}
		return ret;
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
		case "LEADER":
			return p.leader;
		default:
			return null;
		}
	}
}
