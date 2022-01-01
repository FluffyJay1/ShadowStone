package client.ui.game.visualboardanimation.eventanimation.attack;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.*;
import client.Game;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.event.*;

public class EventAnimationDamage extends EventAnimation<EventDamage> {
    /*
     * So this is weird. If the EventDamage has a source, we want to draw the source
     * effect shooting something at the victims. However, if the source is null,
     * there is nothing to do the shooting so we just straight up damage it. If the
     * EventDamage had a source, then the VisualBoard should've used that effect's
     * special animation for damage, and that special animation doesn't have to
     * consider the null source case. Since this animation serves as a default, it
     * is the only class that has to handle both cases.
     */
    public EventAnimationDamage() {
        this(0, 0.5);
    }

    public EventAnimationDamage(double preTime) {
        this(preTime, 0.5);
    }

    public EventAnimationDamage(double preTime, double postTime) {
        super(preTime, postTime);
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        this.visualBoard = b;
        this.event = event;
        if (event.cardSource != null && this.preTime == 0) {
            this.preTime = 0.25;
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            // do the shooting
            g.setColor(Color.red);
            for (int i = 0; i < this.event.m.size(); i++) {
                Vector2f pos = this.event.m.get(i).uiCard.getFinalPos()
                        .sub(this.event.cardSource.uiCard.getFinalPos()).scale((float) (this.normalizedPre()))
                        .add(this.event.cardSource.uiCard.getFinalPos());
                g.fillOval(pos.x - 20, pos.y - 20, 40, 40);
            }
            g.setColor(Color.white);
        } else {
            this.drawDamageNumber(g);
        }
    }

    public void drawDamageNumber(Graphics g) {
        // show the damage number thing
        g.setColor(Color.red);
        UnicodeFont font = Game.getFont("Verdana", 96, true, false);
        g.setFont(font);
        float yoff = (float) (Math.min(Math.pow(0.5 - 2 * this.normalizedPost(), 2), 0.25) * 100);
        for (int i = 0; i < this.event.m.size(); i++) {
            String dstring = this.event.damage.get(i) + "";
            g.drawString(dstring, this.event.m.get(i).uiCard.getFinalPos().x - font.getWidth(dstring) / 2,
                    this.event.m.get(i).uiCard.getFinalPos().y - font.getHeight(dstring) + yoff);
        }
        g.setColor(Color.white);
    }
}
