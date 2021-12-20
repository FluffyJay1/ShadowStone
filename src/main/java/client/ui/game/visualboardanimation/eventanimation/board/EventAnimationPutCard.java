package client.ui.game.visualboardanimation.eventanimation.board;

import org.newdawn.slick.*;

import client.ui.game.*;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.card.*;
import server.event.*;

public class EventAnimationPutCard extends EventAnimation<EventPutCard> {
    public EventAnimationPutCard() {
        super(0, 0.5);
    }

    @Override
    public void onStart() {
        EventPutCard e = this.event;
        for (Card c : e.c) {
            if (e.status.equals(CardStatus.BOARD)) {
                c.uiCard.setScale(UIBoard.CARD_SCALE_BOARD);
            } else if (e.status.equals(CardStatus.HAND)) {
                c.uiCard.setScale(UIBoard.CARD_SCALE_HAND);
            }
        }
    }

    @Override
    public void onFinish() {
        // uh maybe add some particles or something
    }

    @Override
    public void draw(Graphics g) {

    }
}
