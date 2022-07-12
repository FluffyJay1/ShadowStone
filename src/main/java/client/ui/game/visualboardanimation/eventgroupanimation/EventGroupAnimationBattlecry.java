package client.ui.game.visualboardanimation.eventgroupanimation;

import client.ui.game.UICard;
import org.newdawn.slick.*;

import client.Game;

public class EventGroupAnimationBattlecry extends EventGroupAnimation {
    public EventGroupAnimationBattlecry() {
        super(0.7);
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("game/battlecry.png");
        UICard uiCard = this.eventgroup.cards.get(0).uiCard;
        for (int i = 0; i < 4; i++) {
            float xoffset = ((38f * i) % 64) - 32;
            float yoffset = (((32 * i) - (float) this.normalizedTime() * 700) % 128) + 64;
            g.drawImage(img, uiCard.getAbsPos().x - img.getWidth() / 2 + xoffset,
                    uiCard.getAbsPos().y - img.getHeight() / 2 + yoffset);
        }
    }
}
