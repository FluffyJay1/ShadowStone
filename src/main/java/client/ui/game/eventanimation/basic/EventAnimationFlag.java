package client.ui.game.eventanimation.basic;

import org.newdawn.slick.*;

import client.Game;
import client.ui.game.eventanimation.EventAnimation;
import server.event.*;

public class EventAnimationFlag extends EventAnimation<EventFlag> {
    public EventAnimationFlag() {
        super(0, 0.6);
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("res/game/flag.png");
        float yoffset = (float) (Math.pow(1 - this.normalizedPost(), 2) * 150) - 50;
        g.drawImage(img, this.event.owner.uiCard.getFinalPos().x - img.getWidth() / 2,
                this.event.owner.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
    }
}
