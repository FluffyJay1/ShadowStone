package client.ui.particle;

import client.ui.Animation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import java.util.List;
import java.util.function.Supplier;

public class ParticleSystemCommon {
    public static final Supplier<EmissionStrategy> BANISH = () -> new EmissionStrategy(
            new IntervalEmissionTimingStrategy(1, 0.12),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/board/banish.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.5)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0.2, new Vector2f(0, 120),
                            () -> new QuadraticInterpolationB(1, 0, 0),
                            () -> new QuadraticInterpolationB(2, 0, 0)
                    ),
                    new RandomAngleEmissionPropertyStrategy(new ConstantInterpolation(0))
            ))
    );
    public static final Supplier<EmissionStrategy> DESTROY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(25),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/board/vapor.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.2, 1)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0.1, new Vector2f(0, 200),
                            () -> new QuadraticInterpolationB(1, 0, 0),
                            () -> new ConstantInterpolation(1.5)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 100)),
                    new CirclePositionEmissionPropertyStrategy(90),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-60, 60))
            ))
    );
}
