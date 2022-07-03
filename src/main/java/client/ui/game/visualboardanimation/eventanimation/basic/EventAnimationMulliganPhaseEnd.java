package client.ui.game.visualboardanimation.eventanimation.basic;

import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import server.event.EventMulliganPhaseEnd;

// blank animation that just adds some delay before the game starts
public class EventAnimationMulliganPhaseEnd extends EventAnimation<EventMulliganPhaseEnd> {
    public EventAnimationMulliganPhaseEnd() {
        super(1, 0.5);
    }

    @Override
    public boolean shouldAnimate() {
        return true;
    }
}
