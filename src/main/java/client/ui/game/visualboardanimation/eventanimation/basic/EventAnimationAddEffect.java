package client.ui.game.visualboardanimation.eventanimation.basic;

import org.newdawn.slick.*;

import client.Game;
import client.VisualBoard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.card.*;
import server.event.*;

public class EventAnimationAddEffect extends EventAnimation<EventAddEffect> {

    public EventAnimationAddEffect() {
        super(0, 0); // default no animation if we're adding effects to invisible things
    }

    @Override
    public void init(VisualBoard b, EventAddEffect event) {
        super.init(b, event);
        for (Card c : event.c) {
            if (c.isVisible()) {
                this.postTime = 0.3; // if we can see one of them, we animate
                break;
            }
        }
    }

    @Override
    public void onProcess() {
        for (int ind = 0; ind < this.event.c.size(); ind++) {
            if (this.event.successful.get(ind)) {
                Card c = this.event.c.get(ind);
                if (c.isVisible()) {
                    return;
                }
            }
        }
        this.postTime = 0;
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("res/game/battlecry.png");
        for (int ind = 0; ind < this.event.c.size(); ind++) {
            if (this.event.successful.get(ind)) {
                Card c = this.event.c.get(ind);
                if (c.isVisible()) {
                    for (int i = 0; i < 4; i++) {
                        float xoffset = (float) Math.random() * 150 - 75;
                        float yoffset = (float) (-this.normalizedPost() * 160) + 80 + (float) Math.random() * 150 - 75;
                        g.drawImage(img, c.uiCard.getAbsPos().x - img.getWidth() / 2 + xoffset,
                                c.uiCard.getAbsPos().y - img.getHeight() / 2 + yoffset);
                    }
                }
            }
        }
    }
}
