package client.ui.game.eventanimation.basic;

import org.newdawn.slick.*;

import client.Game;
import client.ui.game.eventanimation.EventAnimation;
import server.event.*;

public class EventAnimationOnAttacked extends EventAnimation<EventOnAttacked> {
    public EventAnimationOnAttacked() {
        super(0, 0.6);
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("res/game/defend.png");
        float yoffset = (float) ((1 - this.normalizedPost()) * 150) - 50;
        g.drawImage(img, this.event.owner.uiCard.getFinalPos().x - img.getWidth() / 2,
                this.event.owner.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
    }
}
