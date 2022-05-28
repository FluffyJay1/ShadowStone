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
        if (event.c.stream().anyMatch(c -> c.isVisibleTo(b.localteam))) {
            this.postTime = 0.3; // if we can see one of them, we animate
        }
    }

    @Override
    public void onProcess() {
        for (int ind = 0; ind < this.event.c.size(); ind++) {
            if (this.event.successful.get(ind)) {
                Card c = this.event.c.get(ind);
                if (c.isVisibleTo(this.visualBoard.localteam)) {
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
                if (c.isVisibleTo(this.visualBoard.localteam)) {
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
