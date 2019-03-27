package server.card;

import java.awt.Color;
import java.util.*;
import java.util.List;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.*;

import client.tooltip.*;
import server.*;
import server.card.effect.*;
import server.event.*;

public class BoardObject extends Card {

	public BoardObject(Board b, TooltipCard tooltip) {
		super(b, tooltip);
	}

	@Override
	public void drawOnBoard(Graphics g, Vector2f pos, double scale) {
		super.drawOnBoard(g, pos, scale);
		if (this.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
			this.drawStatNumber(g, pos, scale, this.finalStatEffects.getStat(EffectStats.COUNTDOWN),
					new Vector2f(0.3f, 0.3f), new Vector2f(-0.5f, -0.5f), 50, Color.white);
		}
	}

	public boolean isInPlay() {
		return this.alive && this.status.equals(CardStatus.BOARD);
	}

	public List<EventLastWords> lastWords() {
		List<EventLastWords> list = new LinkedList<EventLastWords>();
		for (Effect e : this.getFinalEffects()) {
			EventLastWords temp = e.lastWords();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<EventFlag> onTurnStart() {
		List<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onTurnStart();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<EventFlag> onTurnEnd() {
		List<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onTurnEnd();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<EventFlag> onEnterPlay() {
		List<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onEnterPlay();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public List<EventFlag> onLeavePlay() {
		List<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onLeavePlay();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	@Override
	public String toString() {
		return "BoardObject " + this.tooltip.name + " " + this.cardPosToString() + " ";
	}

}
