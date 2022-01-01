package client.ui.game.visualboardanimation.eventanimation.basic;

import org.newdawn.slick.*;

import client.Game;
import client.VisualBoard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.card.*;
import server.event.*;

public class EventAnimationSetEffectStats extends EventAnimation<EventSetEffectStats> {

    public EventAnimationSetEffectStats() {
        super(0, 0); // default no animation if we're setting stats to invisible things
    }

    @Override
    public void init(VisualBoard b, EventSetEffectStats event) {
        super.init(b, event);
        if (!event.target.owner.status.equals(CardStatus.GRAVEYARD) && !event.target.owner.status.equals(CardStatus.DECK)) {
            this.postTime = 0.3; // if we can see, we animate
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!this.isPre()) {
            Image img = Game.getImage("res/game/statchange.png").copy();
            Card c = this.event.target.owner;
            for (int i = 0; i < 4; i++) {
                double rotvel = (i - 1.5) * 360;
                img.setRotation((float) (rotvel * this.normalizedPost()));
                g.drawImage(img, c.uiCard.getFinalPos().x - img.getWidth() / 2,
                        c.uiCard.getFinalPos().y - img.getHeight() / 2);
            }
        }
    }
}
