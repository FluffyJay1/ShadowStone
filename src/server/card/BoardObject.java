package server.card;

import java.util.LinkedList;
import java.util.StringTokenizer;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import server.Board;
import server.Player;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.Event;

public class BoardObject extends Card {

	public BoardObject(Board b, CardStatus status, int cost, String name, String text, String imagepath, int team,
			int id) {
		super(b, status, cost, name, text, imagepath, team, id);
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

	public LinkedList<Event> lastWords() {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.lastWords());
		}
		return list;
	}

	public LinkedList<Event> onTurnStart() {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.onTurnStart());
		}
		return list;
	}

	public LinkedList<Event> onTurnEnd() {
		LinkedList<Event> list = new LinkedList<Event>();
		for (Effect e : this.getFinalEffects()) {
			list.addAll(e.onTurnEnd());
		}
		return list;
	}

	public String toString() {
		return "BoardObject " + name + " " + this.cardPosToString() + " ";
	}

}
