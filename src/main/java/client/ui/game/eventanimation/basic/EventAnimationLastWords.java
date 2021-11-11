package client.ui.game.eventanimation.basic;

import org.newdawn.slick.*;

import client.Game;
import client.ui.game.eventanimation.EventAnimation;
import server.event.*;

public class EventAnimationLastWords extends EventAnimation<EventLastWords> {
    public EventAnimationLastWords() {
        super(0, 0.4);
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("res/game/lastwords.png");
        float yoffset = (float) (-this.normalizedPost() * 128) + 64;
        g.drawImage(img, this.event.owner.uiCard.getFinalPos().x - img.getWidth() / 2,
                this.event.owner.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
    }
}
