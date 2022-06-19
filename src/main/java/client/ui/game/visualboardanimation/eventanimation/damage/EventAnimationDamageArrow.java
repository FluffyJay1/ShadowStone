package client.ui.game.visualboardanimation.eventanimation.damage;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.meta.SequentialInterpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;
import client.ui.particle.strategy.timing.meta.DurationLimitingEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationDamageArrow extends EventAnimationDamage {
    private static final Supplier<EmissionStrategy> BARRAGE_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new DurationLimitingEmissionTimingStrategy(0.08, new IntervalEmissionTimingStrategy(1, 0.03)),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/arrow.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.8)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_ADD, 0, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(1, 0, 0),
                            () -> new SequentialInterpolation<>(
                                    List.of(new QuadraticInterpolationA(2, 1, 0.5), new ConstantInterpolation(1)),
                                    List.of(0.07, 0.93)
                            )),
                    new CirclePositionEmissionPropertyStrategy(80),
                    new DirectionalAngleEmissionPropertyStrategy(new LinearInterpolation(-12, 12), new ConstantInterpolation(0))
            ))
    );

    private static final Supplier<EmissionStrategy> FINAL_STRIKE_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(2),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/arrow.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.6)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_ADD, 0, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(1, 0, 0),
                            () -> new SequentialInterpolation<>(
                                    List.of(new QuadraticInterpolationA(4.5, 2, 1.5), new ConstantInterpolation(2)),
                                    List.of(0.07, 0.93)
                            )),
                    new CirclePositionEmissionPropertyStrategy(50),
                    new DirectionalAngleEmissionPropertyStrategy(new LinearInterpolation(-8, 8), new ConstantInterpolation(0))
            ))
    );

    public EventAnimationDamageArrow() {
        super(0.25, true);
    }

    @Override
    public void onStart() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, BARRAGE_EMISSION_STRATEGY.get());
        }
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, FINAL_STRIKE_EMISSION_STRATEGY.get());
        }
    }

    @Override
    public void draw(Graphics g) {
        if (!this.isPre()) {
            this.drawDamageNumber(g);
        }
    }
}
