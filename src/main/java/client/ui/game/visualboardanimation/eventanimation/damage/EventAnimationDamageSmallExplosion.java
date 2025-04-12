package client.ui.game.visualboardanimation.eventanimation.damage;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import server.card.Minion;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import java.util.List;
import java.util.function.Supplier;

// single target xplosinos
public class EventAnimationDamageSmallExplosion extends EventAnimationDamage {
    private static final Supplier<EmissionStrategy> EXPLOSION_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(10),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/misc/fire.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.3, 0.6)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.01, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(1, 0, -2),
                            () -> new QuadraticInterpolationB(1, 0, 8)
                    ),
                    new CirclePositionEmissionPropertyStrategy(150),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 500)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
    );

    public EventAnimationDamageSmallExplosion() {
        super(0, true);
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, EXPLOSION_EMISSION_STRATEGY.get());
        }
    }
}
