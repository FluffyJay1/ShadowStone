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

public class EventAnimationDamageMagicHit extends EventAnimationDamage {
    private static final double CHARGE_TIME = 0.25;
    private static final Supplier<EmissionStrategy> CHARGING_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new DurationLimitingEmissionTimingStrategy(CHARGE_TIME, new IntervalEmissionTimingStrategy(3, 0.05)),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/magichit.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.2, 0.6)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_NORMAL, 0.6, new Vector2f(),
                            () -> new LinearInterpolation(0.5, 0),
                            () -> new LinearInterpolation(0.2, 1)
                    ),
                    new CirclePositionEmissionPropertyStrategy(75),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(-200, -100)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-500, 500))
            ))
    );

    private static final Supplier<EmissionStrategy> BLAST_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(1),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/magicblast.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.25)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationA(1, 0, -1),
                            () -> new QuadraticInterpolationB(2.5, 0.5, 1)
                    ),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-1000, 1000))
            ))
    );

    private static final Supplier<EmissionStrategy> HIT_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(10),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/magichit.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.5, 0.9)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0.65, new Vector2f(0, 0),
                            () -> new LinearInterpolation(0.2, 0),
                            () -> new LinearInterpolation(0.5, 1)
                    ),
                    new CirclePositionEmissionPropertyStrategy(20),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(100, 550)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-100, 100))
            ))
    );

    public EventAnimationDamageMagicHit() {
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
                    new ScaledEmissionStrategy(HIT_EMISSION_STRATEGY.get(), m.uiCard.getScale()));
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD,
                    new ScaledEmissionStrategy(BLAST_EMISSION_STRATEGY.get(), m.uiCard.getScale()));
        }
    }
}
