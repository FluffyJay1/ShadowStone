package client.ui.game.eventanimation;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import server.event.*;

public class EventAnimationUnleash extends EventAnimation {
	public EventAnimationUnleash() {
		this(0.5);
	}

	public EventAnimationUnleash(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventUnleash e = (EventUnleash) this.event;
		Vector2f pos = e.m.uiCard.getFinalPos().sub(e.source.uiCard.getFinalPos())
				.scale((float) (this.normalizedTime())).add(e.source.uiCard.getFinalPos());
		g.setColor(Color.yellow);
		g.fillOval(pos.x - 40, pos.y - 40, 80, 80);
		g.setColor(Color.white);
	}
}
