package client.ui.game.visualboardanimation.eventanimation.destroy;

import java.util.List;
import java.util.function.Supplier;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;

import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.ParticleSystem;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.AnimationEmissionPropertyStrategy;
import client.ui.particle.strategy.property.ComposedEmissionPropertyStrategy;
import client.ui.particle.strategy.property.ConstantEmissionPropertyStrategy;
import client.ui.particle.strategy.property.MaxTimeEmissionPropertyStrategy;
import client.ui.particle.strategy.property.RandomAngleEmissionPropertyStrategy;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import server.card.Card;
import server.event.EventDestroy;

public class EventAnimationDestroyDarkElectro extends EventAnimationDestroy {
    private static final double CHARGE_TIME = 0.3;
    private static final Supplier<EmissionStrategy> CHARGE_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(1),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/attack/darkelectroorb.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(CHARGE_TIME)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationA(0.5, 1, -2),
                            () -> new QuadraticInterpolationA(0, 0.5, -2)
                    ),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-500, -1000))
            ))
    );

    private static final Supplier<EmissionStrategy> BURST_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(1),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/attack/darkelectroburst.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(CHARGE_TIME * 0.5)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 0, new Vector2f(0, 0),
                            () -> new LinearInterpolation(0.5, 1),
                            () -> new QuadraticInterpolationA(0, 0.5, -1.5)
                    ),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(500, 1000))
            ))
    );

    @Override
    public void init(VisualBoard b, EventDestroy event) {
        super.init(b, event);
        this.preTime = CHARGE_TIME;
        this.scheduleAnimation(true, 0.5, this::startBurst);
    }

    @Override
    public void onStart() {
        for (Card c : this.event.cards) {
            ParticleSystem ps = this.visualBoard.uiBoard.addParticleSystem(c.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, CHARGE_EMISSION_STRATEGY.get());
            ps.followElement(c.uiCard, 1);
        }
    }

    public void startBurst() {
        for (Card c : this.event.cards) {
            ParticleSystem ps = this.visualBoard.uiBoard.addParticleSystem(c.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, BURST_EMISSION_STRATEGY.get());
            ps.followElement(c.uiCard, 1);
        }
    }
}
