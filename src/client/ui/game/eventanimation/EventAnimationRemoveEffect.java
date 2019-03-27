package client.ui.game.eventanimation;

import org.newdawn.slick.*;

import client.Game;
import server.event.*;

public class EventAnimationRemoveEffect extends EventAnimation {
	public EventAnimationRemoveEffect() {
		this(0.3);
	}

	public EventAnimationRemoveEffect(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventRemoveEffect e = (EventRemoveEffect) this.event;
		for (int i = 0; i < 4; i++) {
			Image img = Game.getImage("res/game/battlecry.png");
			float xoffset = (float) Math.random() * 150 - 75;
			float yoffset = (float) (this.normalizedTime() * 160) - 80 + (float) Math.random() * 150 - 75;
			g.drawImage(img, e.c.uiCard.getFinalPos().x - img.getWidth() / 2 + xoffset,
					e.c.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
		}
	}
}
