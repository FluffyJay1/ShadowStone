package client.ui.game.eventanimation;

import org.newdawn.slick.*;

import client.Game;
import server.event.*;

public class EventAnimationOnAttacked extends EventAnimation {
	public EventAnimationOnAttacked() {
		this(0.6);
	}

	public EventAnimationOnAttacked(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventOnAttacked e = (EventOnAttacked) this.event;
		Image img = Game.getImage("res/game/defend.png");
		float yoffset = (float) ((1 - this.normalizedTime()) * 150) - 50;
		g.drawImage(img, e.effect.owner.uiCard.getFinalPos().x - img.getWidth() / 2,
				e.effect.owner.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
	}
}
