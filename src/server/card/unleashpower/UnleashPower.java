package server.card.unleashpower;

import java.awt.Color;
import java.util.LinkedList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.geom.Vector2f;

import client.Game;
import client.tooltip.*;
import server.Board;
import server.Player;
import server.card.Card;
import server.card.CardStatus;
import server.card.ClassCraft;
import server.card.Minion;
import server.card.effect.Effect;
import server.card.effect.EffectStats;
import server.event.Event;
import server.event.EventManaChange;
import server.event.EventUnleash;

public class UnleashPower extends Card {
	public static final double UNLEASH_POWER_RADIUS = 50;

	public Player p;
	public int unleashesThisTurn = 0;
	// Vector2f artFocusPos;
	// double artFocusScale;
	Image subImage;

	public UnleashPower(Board b, int team, TooltipUnleashPower tooltip, Vector2f artFocusPos, double artFocusScale) {
		super(b, team, tooltip);

		Image scaledCopy = Game.getImage(imagepath).getScaledCopy((float) (artFocusScale));
		this.subImage = scaledCopy.getSubImage((int) (artFocusPos.x * artFocusScale - UNLEASH_POWER_RADIUS),
				(int) (artFocusPos.y * artFocusScale - UNLEASH_POWER_RADIUS), (int) (UNLEASH_POWER_RADIUS * 2),
				(int) (UNLEASH_POWER_RADIUS * 2));
		this.p = b.getPlayer(team);
	}

	@Override
	public void draw(Graphics g) {
		Image scaledCopy = this.subImage.getScaledCopy((float) this.scale);
		Circle c = new Circle(this.pos.x, this.pos.y, (float) (UNLEASH_POWER_RADIUS * this.scale));
		g.texture(c, scaledCopy, true);
		int stat = this.finalBasicStatEffects.getStat(EffectStats.COST);
		UnicodeFont font = Game.getFont("Verdana", STAT_DEFAULT_SIZE * this.scale, true, false, Color.WHITE,
				Color.BLACK);
		font.drawString((float) (this.pos.x - font.getWidth("" + stat) * 0.5),
				(float) (this.pos.y - UNLEASH_POWER_RADIUS * (float) this.scale), "" + stat);
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
