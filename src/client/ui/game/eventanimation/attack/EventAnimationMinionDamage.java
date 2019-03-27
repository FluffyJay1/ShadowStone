package client.ui.game.eventanimation.attack;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.game.eventanimation.*;
import server.event.*;

public class EventAnimationMinionDamage extends EventAnimation {
	public EventAnimationMinionDamage() {
		this(0.25);
	}

	public EventAnimationMinionDamage(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventMinionDamage e = (EventMinionDamage) this.event;
		g.setColor(Color.red);
		for (int i = 0; i < e.m2.size(); i++) {
			Vector2f pos = e.m2.get(i).uiCard.getFinalPos().sub(e.m1.uiCard.getFinalPos())
					.scale((float) (this.normalizedTime())).add(e.m1.uiCard.getFinalPos());
			g.fillOval(pos.x - 20, pos.y - 20, 40, 40);
		}
		g.setColor(Color.white);
	}
}
