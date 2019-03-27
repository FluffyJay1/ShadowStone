package client.ui.game.eventanimation.attack;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.game.eventanimation.*;
import server.event.*;

public class EventAnimationMinionAttackDamage extends EventAnimation {
	public EventAnimationMinionAttackDamage() {
		this(0.5);
	}

	public EventAnimationMinionAttackDamage(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventMinionAttackDamage e = (EventMinionAttackDamage) this.event;
		Vector2f pos = e.m2.uiCard.getFinalPos().sub(e.m1.uiCard.getFinalPos()).scale((float) (this.normalizedTime()))
				.add(e.m1.uiCard.getFinalPos());
		Vector2f pos2 = e.m2.uiCard.getFinalPos().sub(e.m1.uiCard.getFinalPos())
				.scale(1 - (float) (this.normalizedTime())).add(e.m1.uiCard.getFinalPos());
		g.fillOval(pos.x - 20, pos.y - 20, 40, 40);
		g.fillOval(pos2.x - 20, pos2.y - 20, 40, 40);
	}
}
