package server.card.unleashpower;

import java.util.*;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.Game;
import client.tooltip.*;
import server.*;
import server.card.*;
import server.card.effect.*;
import server.event.*;

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
	public void draw(Graphics g) {
		if (this.subImage == null) {
			Image scaledCopy = Game.getImage(imagepath).getScaledCopy((float) (this.artFocusScale));
			this.subImage = scaledCopy.getSubImage(
					(int) (this.artFocusPos.x * this.artFocusScale - UNLEASH_POWER_RADIUS),
					(int) (this.artFocusPos.y * this.artFocusScale - UNLEASH_POWER_RADIUS),
					(int) (UNLEASH_POWER_RADIUS * 2), (int) (UNLEASH_POWER_RADIUS * 2));
		}
		Image scaledCopy = this.subImage.getScaledCopy((float) this.scale);
		Circle c = new Circle(this.pos.x, this.pos.y, (float) (UNLEASH_POWER_RADIUS * this.scale));
		g.texture(c, scaledCopy, true);

		this.drawCostStat(g, this.finalStatEffects.getStat(EffectStats.COST),
				this.finalBasicStatEffects.getStat(EffectStats.COST), new Vector2f(0, -0.25f), new Vector2f(0, 0.5f),
				STAT_DEFAULT_SIZE);
	}

	// this returns a linkedlist event because fuck u
	public LinkedList<Event> unleash(Minion m) {
		LinkedList<Event> list = new LinkedList<Event>();
		list.add(new EventUnleash(this, m));
		return list;
	}

	@Override
	public boolean isInside(Vector2f p) {
		return p.distance(this.pos) <= UNLEASH_POWER_RADIUS * this.scale;
	}

}
