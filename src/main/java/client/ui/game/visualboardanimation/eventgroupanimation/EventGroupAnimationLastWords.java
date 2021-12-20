package client.ui.game.visualboardanimation.eventgroupanimation;

import client.ui.game.UICard;
import org.newdawn.slick.*;

import client.Game;

public class EventGroupAnimationLastWords extends EventGroupAnimation {
    public EventGroupAnimationLastWords() {
        super(0.4);
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("res/game/lastwords.png");
        UICard uiCard = this.eventgroup.cards.get(0).uiCard;
        float yoffset = (float) (-this.normalizedTime() * 128) + 64;
        g.drawImage(img, uiCard.getFinalPos().x - img.getWidth() / 2,
                uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
    }
}
