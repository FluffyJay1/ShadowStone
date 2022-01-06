package client.ui.game.visualboardanimation.eventanimation.basic;

import org.newdawn.slick.*;

import client.Game;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.event.*;

public class EventAnimationRestore extends EventAnimation<EventRestore> {
    public EventAnimationRestore() {
        super(0, 0.5);
    }

    @Override
    public void draw(Graphics g) {
        if (this.processedEvent) {
            g.setColor(Color.green);
            UnicodeFont font = Game.getFont("Verdana", 80, true, false);
            g.setFont(font);
            float yoff = (float) (Math.pow(1 - this.normalizedPost(), 2) * 50) - 12.5f;
            for (int i = 0; i < this.event.m.size(); i++) {
                String dstring = this.event.actualHeal.get(i) + "";
                g.drawString(dstring, this.event.m.get(i).uiCard.getAbsPos().x - font.getWidth(dstring) / 2,
                        this.event.m.get(i).uiCard.getAbsPos().y - font.getHeight(dstring) + yoff);
            }
            g.setColor(Color.white);
        }
    }
}
