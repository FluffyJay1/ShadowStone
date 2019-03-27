package client.ui.game.eventanimation;

import org.newdawn.slick.*;

import client.Game;
import server.event.*;

public class EventAnimationOnAttack extends EventAnimation {
	public EventAnimationOnAttack() {
		this(0.6);
	}

	public EventAnimationOnAttack(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventOnAttack e = (EventOnAttack) this.event;
		Image img = Game.getImage("res/game/attack.png");
		float yoffset = (float) ((1 - this.normalizedTime()) * 150) - 50;
		g.drawImage(img, e.effect.owner.uiCard.getFinalPos().x - img.getWidth() / 2,
				e.effect.owner.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
	}
}
