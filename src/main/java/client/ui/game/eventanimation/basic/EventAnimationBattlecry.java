package client.ui.game.eventanimation.basic;

import org.newdawn.slick.*;

import client.Game;
import client.ui.game.eventanimation.EventAnimation;
import server.event.*;

public class EventAnimationBattlecry extends EventAnimation<EventBattlecry> {
    public EventAnimationBattlecry() {
        super(0, 0.7);
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("res/game/battlecry.png");
        for (int i = 0; i < 4; i++) {
            float xoffset = ((38f * i) % 64) - 32;
            float yoffset = (((32 * i) - (float) this.normalizedPost() * 700) % 128) + 64;
            g.drawImage(img, this.event.owner.uiCard.getFinalPos().x - img.getWidth() / 2 + xoffset,
                    this.event.owner.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
        }
    }
}
