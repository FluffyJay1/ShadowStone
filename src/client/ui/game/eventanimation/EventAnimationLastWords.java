package client.ui.game.eventanimation;

import org.newdawn.slick.*;

import client.Game;
import server.event.*;

public class EventAnimationLastWords extends EventAnimation {
	public EventAnimationLastWords() {
		this(0.4);
	}

	public EventAnimationLastWords(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventLastWords e = (EventLastWords) this.event;
		Image img = Game.getImage("res/game/lastwords.png");
		float yoffset = (float) (-this.normalizedTime() * 128) + 64;
		g.drawImage(img, e.effect.owner.uiCard.getFinalPos().x - img.getWidth() / 2,
				e.effect.owner.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
	}
}
