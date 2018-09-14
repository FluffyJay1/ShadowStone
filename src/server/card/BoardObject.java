package server.card;

import java.util.LinkedList;
import java.util.StringTokenizer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import client.tooltip.Tooltip;
import server.Board;
import server.Player;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.*;

public class BoardObject extends Card {

	public BoardObject(Board b, CardStatus status, Tooltip tooltip, String imagepath, int team, int id) {
		super(b, status, tooltip, imagepath, team, id);
	}

	@Override
	public void drawOnBoard(Graphics g) {
		super.drawOnBoard(g);
		if (this.finalStatEffects.getUse(EffectStats.COUNTDOWN)) {
			this.drawStatNumber(g, this.finalStatEffects.getStat(EffectStats.COUNTDOWN),
					this.finalStatEffects.getStat(EffectStats.COUNTDOWN), false, new Vector2f(0.3f, 0.3f),
					new Vector2f(-0.5f, -0.5f), 50);
		}
	}

	public boolean isInPlay() {
		return this.alive && this.status.equals(CardStatus.BOARD);
	}

	public LinkedList<EventLastWords> lastWords() {
		LinkedList<EventLastWords> list = new LinkedList<EventLastWords>();
		for (Effect e : this.getFinalEffects()) {
			EventLastWords temp = e.lastWords();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public LinkedList<EventFlag> onTurnStart() {
		LinkedList<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onTurnStart();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public LinkedList<EventFlag> onTurnEnd() {
		LinkedList<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onTurnEnd();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public LinkedList<EventFlag> onEnterPlay() {
		LinkedList<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onEnterPlay();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public LinkedList<EventFlag> onLeavePlay() {
		LinkedList<EventFlag> list = new LinkedList<EventFlag>();
		for (Effect e : this.getFinalEffects()) {
			EventFlag temp = e.onLeavePlay();
			if (temp != null) {
				list.add(temp);
			}
		}
		return list;
	}

	public String toString() {
		return "BoardObject " + this.tooltip.name + " " + this.cardPosToString() + " ";
	}

}
