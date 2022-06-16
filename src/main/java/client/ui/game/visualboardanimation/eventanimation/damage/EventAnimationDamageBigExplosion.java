package client.ui.game.visualboardanimation.eventanimation.damage;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import java.util.List;
import java.util.function.Supplier;

// for explosions that damage adjacent minions
// assumes the center of the explosion is the 0th target
public class EventAnimationDamageBigExplosion extends EventAnimationDamage {
    private static final Supplier<EmissionStrategy> EXPLOSION_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(20),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/misc/fire.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.3, 0.8)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.01, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(1, 0, -2),
                            () -> new QuadraticInterpolationB(1, 0, 8)
                    ),
                    new CirclePositionEmissionPropertyStrategy(150),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 1000)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
    );

    public EventAnimationDamageBigExplosion() {
        super(0, true);
    }

    @Override
    public void onProcess() {
        UICard c = this.event.m.get(0).uiCard;
        this.visualBoard.uiBoard.addParticleSystem(c.getPos(), UIBoard.PARTICLE_Z_BOARD, EXPLOSION_EMISSION_STRATEGY.get());
    }
}
