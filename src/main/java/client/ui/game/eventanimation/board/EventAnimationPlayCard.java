package client.ui.game.eventanimation.board;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.game.*;
import client.ui.game.eventanimation.EventAnimation;
import server.event.*;

public class EventAnimationPlayCard extends EventAnimation<EventPlayCard> {
    public EventAnimationPlayCard() {
        super(0.7, 0);
    }

    @Override
    public void onStart() {
        this.visualBoard.uiBoard.visualPlayingCard = ((EventPlayCard) this.event).c.uiCard;
        this.visualBoard.uiBoard.visualPlayingCard.setScale(UIBoard.CARDS_SCALE_PLAY);
        this.visualBoard.uiBoard.visualPlayingCard.setPos(new Vector2f(0, 0), 0.999f);
        this.visualBoard.uiBoard.visualPlayingCard.setZ(UIBoard.CARD_VISUALPLAYING_Z);
    }

    @Override
    public void onFinish() {
        // uh maybe add some particles or something
    }

    @Override
    public void draw(Graphics g) {

    }
}
