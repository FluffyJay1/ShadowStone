package client.ui.game.eventanimation;

import org.newdawn.slick.*;

import client.Game;
import server.card.*;
import server.event.*;

public class EventAnimationAddEffect extends EventAnimation {
	public EventAnimationAddEffect() {
		this(0.3);
	}

	public EventAnimationAddEffect(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventAddEffect e = (EventAddEffect) this.event;
		for (Card c : e.c) {
			for (int i = 0; i < 4; i++) {
				Image img = Game.getImage("res/game/battlecry.png");
				float xoffset = (float) Math.random() * 150 - 75;
				float yoffset = (float) (-this.normalizedTime() * 160) + 80 + (float) Math.random() * 150 - 75;
				g.drawImage(img, c.uiCard.getFinalPos().x - img.getWidth() / 2 + xoffset,
						c.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
			}
		}
	}
}
