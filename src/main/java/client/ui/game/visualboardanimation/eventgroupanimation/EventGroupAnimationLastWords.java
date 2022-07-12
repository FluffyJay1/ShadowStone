package client.ui.game.visualboardanimation.eventgroupanimation;

import client.VisualBoard;
import client.ui.game.UICard;
import org.newdawn.slick.*;

import client.Game;
import server.card.CardStatus;
import server.event.eventgroup.EventGroup;

public class EventGroupAnimationLastWords extends EventGroupAnimation {
    public EventGroupAnimationLastWords() {
        super(0.4);
    }

    @Override
    public boolean shouldAnimate() {
        return this.eventgroup.cards.stream().anyMatch(c -> c.status.equals(CardStatus.GRAVEYARD) || c.isVisibleTo(this.visualBoard.getLocalteam()));
    }

    @Override
    public void draw(Graphics g) {
        Image img = Game.getImage("game/lastwords.png");
        UICard uiCard = this.eventgroup.cards.get(0).uiCard;
        float yoffset = (float) (-this.normalizedTime() * 128) + 64;
        g.drawImage(img, uiCard.getAbsPos().x - img.getWidth() / 2,
                uiCard.getAbsPos().y - img.getHeight() / 2 + yoffset);
    }
}
