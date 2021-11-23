package client.ui.game.visualboardanimation.eventanimation.attack;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.game.visualboardanimation.eventanimation.*;
import server.event.*;

public class EventAnimationMinionAttack extends EventAnimation<EventMinionAttack> {
    public EventAnimationMinionAttack() {
        super(0, 0.2);

    }

    @Override
    public void draw(Graphics g) {
        Vector2f pos = this.event.m2.uiCard.getFinalPos().sub(this.event.m1.uiCard.getFinalPos())
                .scale((float) (this.normalizedPost())).add(this.event.m1.uiCard.getFinalPos());
        g.fillOval(pos.x - 5, pos.y - 5, 10, 10);
    }

}
