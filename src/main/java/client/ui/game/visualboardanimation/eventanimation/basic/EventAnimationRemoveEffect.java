package client.ui.game.visualboardanimation.eventanimation.basic;

import org.newdawn.slick.*;

import client.Game;
import client.VisualBoard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.card.effect.*;
import server.event.*;

public class EventAnimationRemoveEffect extends EventAnimation<EventRemoveEffect> {
    public EventAnimationRemoveEffect() {
        super(0, 0.3);
    }

    @Override
    public boolean shouldAnimate() {
        return this.event.effects.stream().anyMatch(e -> e.owner.isVisibleTo(this.visualBoard.getLocalteam()));
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("game/battlecry.png");
        for (int i = 0; i < 4; i++) {
            float xoffset = (float) Math.random() * 150 - 75;
            float yoffset = (float) (this.normalizedPost() * 160) - 80 + (float) Math.random() * 150 - 75;
            for (Effect e : this.event.effects) {
                if (e.owner.isVisibleTo(this.visualBoard.getLocalteam())) {
                    g.drawImage(img, e.owner.uiCard.getAbsPos().x - img.getWidth() / 2 + xoffset,
                            e.owner.uiCard.getAbsPos().y - img.getHeight() / 2 + yoffset);
                }
            }
        }
    }
}
