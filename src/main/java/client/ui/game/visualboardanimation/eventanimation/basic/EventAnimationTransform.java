package client.ui.game.visualboardanimation.eventanimation.basic;

import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Card;
import server.card.CardStatus;
import server.event.EventTransform;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationTransform extends EventAnimation<EventTransform> {
    private static final Supplier<EmissionStrategy> DUST_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(16),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/misc/dust.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.5, 1)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0.1, new Vector2f(0, 300),
                            () -> new QuadraticInterpolationA(0, 0, -8),
                            () -> new QuadraticInterpolationA(1, 0, -17)
                    ),
                    new CirclePositionEmissionPropertyStrategy(110),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 50)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-400, 400))
            ))
    );

    public EventAnimationTransform() {
        super(0, 0);
    }

    @Override
    public void init(VisualBoard b, EventTransform event) {
        super.init(b, event);
        if (event.cards.stream().anyMatch(c -> c.isVisibleTo(b.getLocalteam()))) {
            this.preTime = 0.2;
            this.postTime = 0.2;
        }
    }

    @Override
    public void onStart() {
        for (Card c : this.event.cards) {
            if (c.isVisibleTo(this.visualBoard.getLocalteam())) {
                UICard uic = c.uiCard;
                this.visualBoard.uiBoard.addParticleSystem(uic.getPos(),
                        c.status.equals(CardStatus.BOARD) ? UIBoard.PARTICLE_Z_BOARD : UIBoard.PARTICLE_Z_SPECIAL,
                        new ScaledEmissionStrategy(DUST_EMISSION_STRATEGY.get(), uic.getScale()));
            }
        }
    }

    @Override
    public void onProcess() {
        for (Card c : this.event.into) {
            if (c.isVisibleTo(this.visualBoard.getLocalteam())) {
                UICard transformUIC = c.uiCard;
                transformUIC.setVisible(true);
                Vector2f destPos = this.visualBoard.uiBoard.getBoardPosFor(c.getIndex(), c.team, this.visualBoard.getPlayer(c.team).getPlayArea().size());
                transformUIC.setPos(destPos, 1);
            }
        }
        this.visualBoard.uiBoard.refreshAnimatedTargets();
    }
}
