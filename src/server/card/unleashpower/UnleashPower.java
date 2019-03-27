package server.card.unleashpower;

import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import client.VisualBoard;
import client.tooltip.TooltipUnleashPower;
import server.Board;
import server.Player;
import server.card.Card;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.Event;
import server.event.EventUnleash;

public class UnleashPower extends Card {
	public static final double UNLEASH_POWER_RADIUS = 50;

	public Player p;
	public int unleashesThisTurn = 0;
	Vector2f artFocusPos;
	double artFocusScale;
	Image subImage;

	public UnleashPower(Board b, TooltipUnleashPower tooltip, Vector2f artFocusPos, double artFocusScale) {
		super(b, tooltip);
		Effect e = new Effect(0, "", tooltip.cost);
		e.set.setStat(EffectStats.ATTACKS_PER_TURN, 1);
		this.addBasicEffect(e);
		this.artFocusPos = artFocusPos;
		this.artFocusScale = artFocusScale;
	}

	@Override
	public void draw(Graphics g, Vector2f pos, double scale) {
		if (this.subImage == null) {
			Image scaledCopy = Game.getImage(imagepath).getScaledCopy((float) (this.artFocusScale));
			this.subImage = scaledCopy.getSubImage(
					(int) (this.artFocusPos.x * this.artFocusScale - UNLEASH_POWER_RADIUS),
					(int) (this.artFocusPos.y * this.artFocusScale - UNLEASH_POWER_RADIUS),
					(int) (UNLEASH_POWER_RADIUS * 2), (int) (UNLEASH_POWER_RADIUS * 2));
		}
		Image scaledCopy = this.subImage.getScaledCopy((float) scale);
		Circle c = new Circle(pos.x, pos.y, (float) (UNLEASH_POWER_RADIUS * scale));
		g.texture(c, scaledCopy, true);

		this.drawCostStat(g, pos, scale, this.finalStatEffects.getStat(EffectStats.COST),
				this.finalBasicStatEffects.getStat(EffectStats.COST), new Vector2f(0, -0.25f), new Vector2f(0, 0.5f),
				STAT_DEFAULT_SIZE);

		if (this.board instanceof VisualBoard && this.board.getPlayer(this.team).realPlayer.canUnleash()
				&& !((VisualBoard) this.board).disableInput) {
			g.setColor(Color.cyan);
			g.drawOval((float) (pos.x - UNLEASH_POWER_RADIUS * scale), (float) (pos.y - UNLEASH_POWER_RADIUS * scale),
					(float) (UNLEASH_POWER_RADIUS * 2 * scale), (float) (UNLEASH_POWER_RADIUS * 2 * scale));
			g.setColor(Color.white);
		}
	}

	// this returns a linkedlist event because fuck u
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		return list;
	}

}
