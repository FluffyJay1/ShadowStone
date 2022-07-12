package client.ui.game.visualboardanimation.eventanimation.basic;

import org.newdawn.slick.*;

import client.Game;
import client.VisualBoard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.card.*;
import server.event.*;

public class EventAnimationAddEffect extends EventAnimation<EventAddEffect> {

    public EventAnimationAddEffect() {
        super(0, 0.3);
    }

    @Override
    public boolean shouldAnimate() {
        return this.event.c.stream().anyMatch(c -> c.isVisibleTo(this.visualBoard.getLocalteam()));
    }

    @Override
    public void onProcess() {
        for (int ind = 0; ind < this.event.c.size(); ind++) {
            if (this.event.successful.get(ind)) {
                Card c = this.event.c.get(ind);
                if (c.isVisibleTo(this.visualBoard.getLocalteam())) {
                    return;
                }
            }
        }
        this.postTime = 0;
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("game/battlecry.png");
        for (int ind = 0; ind < this.event.c.size(); ind++) {
            if (this.event.successful.get(ind)) {
                Card c = this.event.c.get(ind);
                if (c.isVisibleTo(this.visualBoard.getLocalteam())) {
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
