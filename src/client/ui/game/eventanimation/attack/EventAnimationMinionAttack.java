package client.ui.game.eventanimation.attack;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.game.eventanimation.*;
import server.event.*;

public class EventAnimationMinionAttack extends EventAnimation {

	public EventAnimationMinionAttack() {
		this(0.2);
	}

	public EventAnimationMinionAttack(double duration) {
		super(duration);

	}

	@Override
	public void draw(Graphics g) {
		EventMinionAttack e = (EventMinionAttack) this.event;
		Vector2f pos = e.m2.uiCard.getFinalPos().sub(e.m1.uiCard.getFinalPos()).scale((float) (this.normalizedTime()))
				.add(e.m1.uiCard.getFinalPos());
		g.fillOval(pos.x - 5, pos.y - 5, 10, 10);
	}

}
