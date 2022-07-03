package client.ui.game.visualboardanimation.eventanimation.basic;

import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.ParticleSystem;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;
import client.ui.particle.strategy.timing.meta.DurationLimitingEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Card;
import server.event.EventSpellboost;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class EventAnimationSpellboost extends EventAnimation<EventSpellboost> {
    private static final Supplier<EmissionStrategy> SPELLBOOST_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new DurationLimitingEmissionTimingStrategy(0.3, new IntervalEmissionTimingStrategy(4, 0.01)),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/game/battlecry.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.1, 0.2)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.7, new Vector2f(),
                            () -> new QuadraticInterpolationB(0.5, 0, 0),
                            () -> new ConstantInterpolation(1)
                    ),
                    new DirectionalVelocityEmissionPropertyStrategy(new Vector2f(0, -1), new QuadraticInterpolationB(400, 2000, 0)),
                    new DirectionalPositionEmissionPropertyStrategy(new Vector2f(0, 1), new LinearInterpolation(0, 50)),
                    new CirclePositionEmissionPropertyStrategy(100)
            ))
    );

    public EventAnimationSpellboost() {
        super(0, 0.2);
    }

    @Override
    public boolean shouldAnimate() {
        return IntStream.range(0, this.event.cards.size()).anyMatch(this::shouldAnimate);
    }

    private boolean shouldAnimate(int i) {
        Card c = this.event.cards.get(i);
        return c.isVisibleTo(this.visualBoard.getLocalteam());
    }

    public void onProcess() {
        for (int i = 0; i < this.event.cards.size(); i++) {
            if (this.shouldAnimate(i)) {
                Card c = this.event.cards.get(i);
                UICard uic = c.uiCard;
                ParticleSystem p = this.visualBoard.uiBoard.addParticleSystem(uic.getPos(), UIBoard.PARTICLE_Z_SPECIAL,
                        new ScaledEmissionStrategy(SPELLBOOST_EMISSION_STRATEGY.get(), uic.getScale()));
                p.followElement(uic, 1);
            }
        }
    }
}
