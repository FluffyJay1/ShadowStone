package client.ui.game.visualboardanimation.eventanimation.basic;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.event.*;

public class EventAnimationUnleash extends EventAnimation<EventUnleash> {
    public EventAnimationUnleash() {
        super(0, 0.5);
    }

    @Override
    public void draw(Graphics g) {
        Vector2f pos = this.event.m.uiCard.getFinalPos().sub(this.event.source.uiCard.getFinalPos())
                .scale((float) (this.normalizedPost())).add(this.event.source.uiCard.getFinalPos());
        g.setColor(Color.yellow);
        g.fillOval(pos.x - 40, pos.y - 40, 80, 80);
        g.setColor(Color.white);
    }
}
