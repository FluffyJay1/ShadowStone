package client.ui.game.eventanimation.basic;

import org.newdawn.slick.*;

import client.Game;
import client.VisualBoard;
import client.ui.game.eventanimation.EventAnimation;
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
            if (!c.status.equals(CardStatus.GRAVEYARD) && !c.status.equals(CardStatus.DECK)) {
                this.postTime = 0.3; // if we can see one of them, we animate
                break;
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("res/game/battlecry.png");
        for (Card c : this.event.c) {
            if (!c.status.equals(CardStatus.GRAVEYARD) && !c.status.equals(CardStatus.DECK)) {
                for (int i = 0; i < 4; i++) {
                    float xoffset = (float) Math.random() * 150 - 75;
                    float yoffset = (float) (-this.normalizedPost() * 160) + 80 + (float) Math.random() * 150 - 75;
                    g.drawImage(img, c.uiCard.getFinalPos().x - img.getWidth() / 2 + xoffset,
                            c.uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
                }
            }
        }
    }
}
