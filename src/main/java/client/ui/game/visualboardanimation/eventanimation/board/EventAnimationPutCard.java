package client.ui.game.visualboardanimation.eventanimation.board;

import client.VisualBoard;
import org.newdawn.slick.*;

import client.ui.game.*;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import org.newdawn.slick.geom.Vector2f;
import server.card.*;
import server.event.*;

public class EventAnimationPutCard extends EventAnimation<EventPutCard> {
    private static final int EDGE_PADDING = 1;
    private static final float TEAM_OFFSET = 0.06f;
    private static final float DECK_ALIGN_WEIGHT = 0.9f; //0 means left, 1 means right
    public EventAnimationPutCard() {
        super(0, 0.5);
    }

    @Override
    public void init(VisualBoard b, EventPutCard event) {
        super.init(b, event);
        switch (event.status) {
            case BOARD -> {
                this.postTime = 0.25;
            }
            case HAND, DECK -> {
                this.preTime = 0.2;
                this.postTime = 0.6;
                this.scheduleAnimation(false, 0.5, this::sendCards);
            }
        }
    }
    @Override
    public void onStart() {
        if (this.event.status.equals(CardStatus.HAND) || this.event.status.equals(CardStatus.DECK)) {
            for (int i = 0; i < this.event.cards.size(); i++) {
                Card c = this.event.cards.get(i);
                UICard uic = c.uiCard;
                this.useCardInAnimation(uic);
                uic.setScale(UICard.SCALE_MOVE);
                uic.setZ(UIBoard.CARD_VISUALPLAYING_Z);
                uic.setFlippedOver(false);
                // fan the cards between [-0.5, 0.5]
                float alignWeight = 0.5f;
                if (c.status.equals(CardStatus.DECK)) {
                    alignWeight = DECK_ALIGN_WEIGHT; // put cards closer to the deck
                }
                float fanX = (float) ((1 - this.visualBoard.uiBoard.getWidthInRel(uic.getWidth(false)))
                        * (i + alignWeight - this.event.cards.size() / 2.)) / (this.event.cards.size() - 1 + EDGE_PADDING * 2);
                float fanY = c.team == this.visualBoard.localteam ? TEAM_OFFSET : -TEAM_OFFSET;
                uic.setPos(new Vector2f(fanX, fanY), 0.999);
            }
        }
    }

    @Override
    public void onProcess() {
    }

    private void sendCards() {
        for (int i = 0; i < this.event.cards.size(); i++) {
            Card c = this.event.cards.get(i);
            UICard uic = c.uiCard;
            if (this.event.successful.get(i) || !this.event.attempted.get(i)) {
                this.stopUsingCardInAnimation(uic);
            } else {
                // it's gonna get destroyed, don't let the uiboard mess with it
                uic.useInAnimation();
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
