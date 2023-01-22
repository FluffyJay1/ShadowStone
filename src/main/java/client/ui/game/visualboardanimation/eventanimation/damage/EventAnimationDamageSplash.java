package client.ui.game.visualboardanimation.eventanimation.damage;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.ParticleSystem;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;
import client.ui.particle.strategy.timing.meta.DurationLimitingEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationDamageSplash extends EventAnimationDamage {
    private static final double CHARGE_TIME = 0.15;
    private static final Supplier<EmissionStrategy> CHARGING_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new DurationLimitingEmissionTimingStrategy(CHARGE_TIME, new IntervalEmissionTimingStrategy(2, 0.01)),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/attack/water.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.1, 0.2)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_NORMAL, 0.6, new Vector2f(0, -300),
                            () -> new LinearInterpolation(1, 0),
                            () -> new LinearInterpolation(0, 1)
                    ),
                    new CirclePositionEmissionPropertyStrategy(100),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(-200, -250)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-500, 500))
            ))
    );
    private static final Supplier<EmissionStrategy> SPLASH_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(10),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/attack/water.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.1, 0.6)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.01, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(1, 0, 0),
                            () -> new QuadraticInterpolationA(1 + Math.random() * 0.5, 1.5 + Math.random() * 2, -1)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new QuadraticInterpolationB(0, 300, 0)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-10, 10))
            ))
    );

    public EventAnimationDamageSplash() {
        super(CHARGE_TIME, true);
    }

    @Override
    public void onStart() {
        for (Minion m : this.event.m) {
            ParticleSystem ps = this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD,
                    new ScaledEmissionStrategy(CHARGING_EMISSION_STRATEGY.get(), m.uiCard.getScale()));
            ps.followElement(m.uiCard, 1);
        }
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD,
                    new ScaledEmissionStrategy(SPLASH_EMISSION_STRATEGY.get(), m.uiCard.getScale()));
        }
    }
}
