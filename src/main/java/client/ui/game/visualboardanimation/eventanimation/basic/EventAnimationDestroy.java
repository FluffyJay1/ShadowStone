package client.ui.game.visualboardanimation.eventanimation.basic;

import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.particle.ParticleSystemCommon;
import server.card.Card;
import server.event.EventDestroy;

public class EventAnimationDestroy extends EventAnimation<EventDestroy> {
    public EventAnimationDestroy() {
        super(0, 0.1);
    }

    @Override
    public void onProcess() {
        for (Card c : this.event.cards) {
            UICard uic = c.uiCard;
            this.visualBoard.uiBoard.addParticleSystem(uic.getAbsPos(), ParticleSystemCommon.DESTROY.get());
        }
    }
}