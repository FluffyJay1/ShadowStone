package client.ui.game.visualboardanimation.eventanimation.damage;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.LinearInterpolation;
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

public class EventAnimationDamageWind extends EventAnimationDamage {
    private static final double CHARGE_TIME = 0.3;
    private static final Supplier<EmissionStrategy> CHARGING_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new DurationLimitingEmissionTimingStrategy(CHARGE_TIME, new IntervalEmissionTimingStrategy(4, 0.04)),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/attack/wind.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.1, 0.2)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_ADD, 0.6, new Vector2f(0, -300),
                            () -> new LinearInterpolation(1, 0),
                            () -> {
                                double randomoffset = Math.random() * 0.5;
                                return new LinearInterpolation(1.5 + randomoffset, 1 + randomoffset);
                            }
                    ),
                    new DirectionalVelocityEmissionPropertyStrategy(new Vector2f(0, -1), new LinearInterpolation(800, 1000)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(800, 1000))
            ))
    );
    private static final Supplier<EmissionStrategy> EXPLOSION_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(10),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/attack/wind.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.4, 0.7)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.65, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(1, 0, -2),
                            () -> new QuadraticInterpolationB(1, 3 + Math.random(), 4)
                    ),
                    new CirclePositionEmissionPropertyStrategy(20),
                    new RadialVelocityEmissionPropertyStrategy(new QuadraticInterpolationB(0, 350, 0)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
    );

    public EventAnimationDamageWind() {
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
                    new ScaledEmissionStrategy(EXPLOSION_EMISSION_STRATEGY.get(), m.uiCard.getScale()));
        }
    }
}
