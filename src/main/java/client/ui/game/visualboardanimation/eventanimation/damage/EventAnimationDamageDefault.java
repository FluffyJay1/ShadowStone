package client.ui.game.visualboardanimation.eventanimation.damage;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

public class EventAnimationDamageDefault extends EventAnimationDamage {
    public EventAnimationDamageDefault() {
        super(0.25, true);
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            // do the shooting
            g.setColor(Color.red);
            for (int i = 0; i < this.event.m.size(); i++) {
                Vector2f pos = this.event.m.get(i).uiCard.getAbsPos()
                        .sub(this.event.cardSource.uiCard.getAbsPos()).scale((float) (this.normalizedPre()))
                        .add(this.event.cardSource.uiCard.getAbsPos());
                g.fillOval(pos.x - 20, pos.y - 20, 40, 40);
            }
            g.setColor(Color.white);
        } else {
            this.drawDamageNumber(g);
        }
    }
}
