package client.ui.game.eventanimation;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import server.event.*;

public class EventAnimationEffectDamage extends EventAnimation {
	public EventAnimationEffectDamage() {
		this(0.25);
	}

	public EventAnimationEffectDamage(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventEffectDamage e = (EventEffectDamage) this.event;
		g.setColor(Color.red);
		for (int i = 0; i < e.m2.size(); i++) {
			Vector2f pos = e.m2.get(i).uiCard.getFinalPos().sub(e.source.owner.uiCard.getFinalPos())
					.scale((float) (this.normalizedTime())).add(e.source.owner.uiCard.getFinalPos());
			g.fillOval(pos.x - 20, pos.y - 20, 40, 40);
		}
		g.setColor(Color.white);
	}
}
