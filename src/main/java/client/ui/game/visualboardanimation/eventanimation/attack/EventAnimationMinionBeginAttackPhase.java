package client.ui.game.visualboardanimation.eventanimation.attack;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.game.visualboardanimation.eventanimation.*;
import server.event.*;

/*
 * This event was supposed to be like a sentinel, telling us that the next two events 
 * are damage events which should have minion attack animations attached to them. 
 * Animating the sentinel itself should be useless, so this animation should never be
 * used. Still, we can copy the code from this as an example.
 * 
 * Turns out this was solved by grouping events
 */
public class EventAnimationMinionBeginAttackPhase extends EventAnimation<EventMinionBeginAttackPhase> {
    public EventAnimationMinionBeginAttackPhase() {
        super(0, 0.5);
    }

    @Override
    public void draw(Graphics g) {
        EventMinionBeginAttackPhase e = this.event;
        Vector2f pos = e.m2.uiCard.getFinalPos().sub(e.m1.uiCard.getFinalPos()).scale((float) (this.normalizedPost()))
                .add(e.m1.uiCard.getFinalPos());
        Vector2f pos2 = e.m2.uiCard.getFinalPos().sub(e.m1.uiCard.getFinalPos())
                .scale(1 - (float) (this.normalizedPost())).add(e.m1.uiCard.getFinalPos());
        g.fillOval(pos.x - 20, pos.y - 20, 40, 40);
        g.fillOval(pos2.x - 20, pos2.y - 20, 40, 40);
    }
}
