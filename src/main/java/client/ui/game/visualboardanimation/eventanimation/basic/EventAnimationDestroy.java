package client.ui.game.visualboardanimation.eventanimation.basic;

import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.particle.ParticleSystemCommon;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import server.card.Card;
import server.event.EventDestroy;

public class EventAnimationDestroy extends EventAnimation<EventDestroy> {
    public EventAnimationDestroy() {
        super(0, 0.3);
    }

    @Override
    public void onProcess() {
        for (int i = 0; i < this.event.cards.size(); i++) {
            Card c = this.event.cards.get(i);
            if (this.event.successful.get(i)) {
                UICard uic = c.uiCard;
                this.visualBoard.uiBoard.addParticleSystem(uic.getAbsPos(), UIBoard.PARTICLE_Z_BOARD, new ScaledEmissionStrategy(ParticleSystemCommon.DESTROY.get(), uic.getScale()));
            }
        }
    }
}