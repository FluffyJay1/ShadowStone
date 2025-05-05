package client.ui.game.visualboardanimation.eventanimation.destroy;

import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.particle.ParticleSystemCommon;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import server.card.Card;
import server.event.EventDestroy;

public class EventAnimationDestroy extends EventAnimation<EventDestroy> {
    /*
     * If subclasses have no additional parameters, all they need to implement
     * the () constructor, nothing else is needed. If the animation requires
     * some parameters, they will need to implement a
     * extraParamString() and fromExtraParams(StringTokenizer) method.
     */
    private final boolean requireNonEmpty;

    public EventAnimationDestroy() {
        this(0, true);
    }

    public EventAnimationDestroy(double preTime, boolean requireNonEmpty) {
        this(preTime, 0.3, requireNonEmpty);
    }

    public EventAnimationDestroy(double preTime, double postTime, boolean requireNonEmpty) {
        super(preTime, postTime);
        this.requireNonEmpty = requireNonEmpty;
    }

    @Override
    public boolean shouldAnimate() {
        return !requireNonEmpty || !event.cards.isEmpty();
    }

    @Override
    public void onProcess() {
        for (int i = 0; i < this.event.cards.size(); i++) {
            Card c = this.event.cards.get(i);
            if (this.event.successful.get(i)) {
                UICard uic = c.uiCard;
                this.visualBoard.uiBoard.addParticleSystem(uic.getPos(), UIBoard.PARTICLE_Z_BOARD, new ScaledEmissionStrategy(ParticleSystemCommon.DESTROY.get(), uic.getScale()));
            }
        }
    }
}