package client.ui.game.eventanimation;

import org.newdawn.slick.*;

import client.Game;
import server.event.*;

public class EventAnimationBattlecry extends EventAnimation {
	public EventAnimationBattlecry() {
		this(0.7);
	}

	public EventAnimationBattlecry(double duration) {
		super(duration);
	}

	@Override
	public void draw(Graphics g) {
		EventBattlecry e = (EventBattlecry) this.event;
		for (int i = 0; i < 4; i++) {
			Image img = Game.getImage("res/game/battlecry.png");
			float xoffset = ((38f * i) % 64) - 32;
			float yoffset = (((32 * i) - (float) this.normalizedTime() * 700) % 128) + 64;
			g.drawImage(img, e.effect.owner.uiCard.getFinalPos().x - img.getWidth() / 2 + xoffset,
					e.effect.owner.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
		}
	}
}
