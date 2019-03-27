package client.ui.game.eventanimation;

import org.newdawn.slick.*;

import client.Game;
import server.event.*;

public class EventAnimationFlag extends EventAnimation {
	public EventAnimationFlag() {
		this(0.6);
	}

	public EventAnimationFlag(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventFlag e = (EventFlag) this.event;
		Image img = Game.getImage("res/game/flag.png");
		float yoffset = (float) (Math.pow(1 - this.normalizedTime(), 2) * 150) - 50;
		g.drawImage(img, e.effect.owner.uiCard.getFinalPos().x - img.getWidth() / 2,
				e.effect.owner.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
	}
}
