package client.ui.game.visualboardanimation.eventanimation.basic;

import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.particle.ParticleSystemCommon;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import org.newdawn.slick.geom.Vector2f;
import server.card.Card;
import server.event.EventDiscard;

public class EventAnimationDiscard extends EventAnimation<EventDiscard> {
    private static final float MOVE_AMOUNT = 0.2f;

    public EventAnimationDiscard() {
        super(0.5, 0.3);
    }

    @Override
    public void onStart() {
        for (int i = 0; i < this.event.cards.size(); i++) {
            Card c = this.event.cards.get(i);
            UICard uic = c.uiCard;
            this.useCardInAnimation(uic);
            uic.setScale(UICard.SCALE_MOVE);
            uic.setFlippedOver(false);
            uic.draggable = false;
            uic.setPos(uic.getRelPos().add(new Vector2f(0, -MOVE_AMOUNT * c.team * this.visualBoard.getLocalteam())), 0.999);
        }
    }

    @Override
    public void onProcess() {
        for (int i = 0; i < this.event.cards.size(); i++) {
            Card c = this.event.cards.get(i);
            UICard uic = c.uiCard;
            if (this.event.successful.get(i)) {
                this.visualBoard.uiBoard.addParticleSystem(uic.getPos(), UIBoard.PARTICLE_Z_BOARD, new ScaledEmissionStrategy(ParticleSystemCommon.DESTROY.get(), uic.getScale()));
            }
            this.stopUsingCardInAnimation(uic);
        }
    }
}