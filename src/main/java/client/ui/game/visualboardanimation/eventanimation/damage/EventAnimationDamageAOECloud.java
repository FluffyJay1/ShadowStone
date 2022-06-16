package client.ui.game.visualboardanimation.eventanimation.damage;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationDamageAOECloud extends EventAnimationDamage {
    private static final Supplier<EmissionStrategy> CLOUD_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(40),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/purplecloud.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(1)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_NORMAL, 0.2, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(1, 0, 0),
                            () -> new QuadraticInterpolationB(Math.random() + 1, 2 * Math.random() + 5, 8)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(1300, 3000)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
    );

    public EventAnimationDamageAOECloud() {
        super(0.1, false);
    }

    @Override
    public void onStart() {
        this.visualBoard.uiBoard.addParticleSystem(new Vector2f(), UIBoard.PARTICLE_Z_BOARD, CLOUD_EMISSION_STRATEGY.get());
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            // idk draw something here maybe
        } else {
            this.drawDamageNumber(g);
        }
    }
}
