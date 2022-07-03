package client.ui.game.visualboardanimation.eventanimation.basic;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.game.visualboardanimation.eventanimation.EventAnimation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.AnimationEmissionPropertyStrategy;
import client.ui.particle.strategy.property.ComposedEmissionPropertyStrategy;
import client.ui.particle.strategy.property.ConstantEmissionPropertyStrategy;
import client.ui.particle.strategy.property.MaxTimeEmissionPropertyStrategy;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.event.EventNecromancy;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationNecromancy extends EventAnimation<EventNecromancy> {
    private static final Supplier<EmissionStrategy> NECROMANCY_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(1),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/game/necromancy.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.6)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0, new Vector2f(),
                            () -> new QuadraticInterpolationB(1, 0, 0),
                            () -> new LinearInterpolation(1, 4)
                    )
            ))
    );

    public EventAnimationNecromancy() {
        super(0, 0.4);
    }

    @Override
    public boolean shouldAnimate() {
        return true;
    }

    public void onProcess() {
        UICard uic = this.event.source.owner.uiCard;
        this.visualBoard.uiBoard.addParticleSystem(uic.getPos(), UIBoard.PARTICLE_Z_BOARD, NECROMANCY_EMISSION_STRATEGY.get());
    }
}
