package client.ui.game.visualboardanimation.eventanimation.basic;

import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.particle.ParticleSystem;
import client.ui.particle.ParticleSystemCommon;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import server.card.Card;
import server.card.CardStatus;
import server.event.EventBanish;

import java.util.ArrayList;
import java.util.List;

public class EventAnimationBanish extends EventAnimation<EventBanish> {
    private List<ParticleSystem> banishParticles;
    public EventAnimationBanish() {
        super(0.5, 0.25);
    }

    @Override
    public void onStart() {
        this.banishParticles = new ArrayList<>(this.event.cards.size());
        for (Card c : this.event.cards) {
            UICard uic = c.uiCard;
            this.useCardInAnimation(uic);
            int z = c.status.equals(CardStatus.BOARD) ? UIBoard.PARTICLE_Z_BOARD : UIBoard.PARTICLE_Z_SPECIAL;
            ParticleSystem particles = this.visualBoard.uiBoard.addParticleSystem(uic.getPos(), z,
                    new ScaledEmissionStrategy(ParticleSystemCommon.BANISH.get(), uic.getScale()));
            particles.followElement(uic, 1);
            this.banishParticles.add(particles);
        }
    }

    @Override
    public void onProcess() {
        for (ParticleSystem particles : this.banishParticles) {
            particles.kill();
        }
        for (Card c : this.event.cards) {
            // kekbye
            UICard uic = c.uiCard;
            uic.setVisible(false);
        }
    }
}
