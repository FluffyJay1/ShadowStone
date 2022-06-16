package client.ui.game.visualboardanimation.eventanimation.damage;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.color.LinearColorInterpolation;
import client.ui.interpolation.meta.ComposedInterpolation;
import client.ui.interpolation.meta.SequentialInterpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.interpolation.realvalue.SpringInterpolation;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationDamageEnergyBeamQuick extends EventAnimationDamage {
    private static final Interpolation<Color> BEAM_START_COLOR = new ComposedInterpolation<>(
            new SpringInterpolation(1),
            new LinearColorInterpolation(
                    new Color(0.4f, 0.4f, 0.9f, 0.2f),
                    new Color(0.7f, 0.7f, 1.0f, 2.0f)
            )
    );
    private static final Interpolation<Color> BEAM_END_COLOR = new LinearColorInterpolation(
            new Color(1.0f, 1.0f, 1.0f, 1.0f),
            new Color(0.5f, 0.5f, 1.0f, 0.5f)
    );

    private static final Interpolation<Double> BEAM_START_WIDTH = new QuadraticInterpolationB(10, 80, 140);
    private static final Interpolation<Double> BEAM_END_WIDTH = new SequentialInterpolation<>(List.of(
            new QuadraticInterpolationB(200, 0, -100),
            new ConstantInterpolation(0)
    ), List.of(0.2, 0.8));

    private static final Supplier<EmissionStrategy> ENERGY_HIT_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(25),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/misc/energy.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.4, 1.0)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.01, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(1, 0, -2),
                            () -> new ConstantInterpolation(1 + Math.random() * 2)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new QuadraticInterpolationB(1000, 2000, 0))
            ))
    );

    public EventAnimationDamageEnergyBeamQuick() {
        super(0.05, true);
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, ENERGY_HIT_STRATEGY.get());
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            this.drawBeam(g, BEAM_START_WIDTH.get(this.normalizedPre()), BEAM_START_COLOR.get(this.normalizedPre()));
        } else {
            this.drawBeam(g, BEAM_END_WIDTH.get(this.normalizedPost()), BEAM_END_COLOR.get(this.normalizedPost()));
            this.drawDamageNumber(g);
        }
    }
}
