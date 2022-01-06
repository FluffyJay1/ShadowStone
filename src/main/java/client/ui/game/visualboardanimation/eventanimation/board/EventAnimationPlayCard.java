package client.ui.game.visualboardanimation.eventanimation.board;

import org.newdawn.slick.*;
import org.newdawn.slick.geom.*;

import client.ui.game.*;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.event.*;

public class EventAnimationPlayCard extends EventAnimation<EventPlayCard> {
    public EventAnimationPlayCard() {
        super(0.7, 0);
    }

    @Override
    public void onStart() {
        UICard uic = this.event.c.uiCard;
        this.useCardInAnimation(this.event.c.uiCard);
        uic.setScale(UIBoard.CARD_SCALE_PLAY);
        uic.setPos(new Vector2f(0, 0), 0.999f);
        uic.setZ(UIBoard.CARD_VISUALPLAYING_Z);
        uic.draggable = false;
    }

    @Override
    public void onFinish() {
        // uh maybe add some particles or something
    }

    @Override
    public void draw(Graphics g) {

    }
}
