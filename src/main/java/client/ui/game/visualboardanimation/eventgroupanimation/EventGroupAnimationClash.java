package client.ui.game.visualboardanimation.eventgroupanimation;

import client.ui.game.UICard;
import org.newdawn.slick.*;

import client.Game;

public class EventGroupAnimationClash extends EventGroupAnimation {
    public EventGroupAnimationClash() {
        super(0.6);
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("res/game/clash.png");
        UICard uiCard = this.eventgroup.cards.get(0).uiCard;
        float yoffset = (float) (Math.pow(1 - this.normalizedTime(), 2) * 150) - 50;
        g.drawImage(img, uiCard.getFinalPos().x - img.getWidth() / 2,
                uiCard.getFinalPos().y - img.getHeight() / 2 + yoffset);
    }
}
