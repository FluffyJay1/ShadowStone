package client.ui.game.visualboardanimation.eventanimation.damage;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.meta.ComposedInterpolation;
import client.ui.interpolation.realvalue.*;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationDamageOmoriStab extends EventAnimationDamage {
    // these must be suppliers to avoid ExceptionInInitializerError

    private static final Supplier<EmissionStrategy> STAB_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(1),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> {
                        Animation anim = new Animation("animation/stab.png", new Vector2f(1, 6), 0, 0, Image.FILTER_NEAREST);
                        anim.play = true;
                        anim.setFrameIntervals(new double[]{0.4, 0.08, 0.08, 0.08, 0.08, 0.4});
                        return anim;
                    }),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(1.5)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_NORMAL, 1, new Vector2f(0, 0),
                            () -> new ComposedInterpolation<>(new ClampedInterpolation(0.8, 1), new LinearInterpolation(1, 0)),
                            () -> new ConstantInterpolation(2)
                    )
            ))
    );
    private static final Vector2f STAB_OFFSET = new Vector2f(128, 0);

    public EventAnimationDamageOmoriStab () {
        super(0.64, true);
    }

    @Override
    public void onStart() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos().add(STAB_OFFSET), UIBoard.PARTICLE_Z_BOARD, STAB_EMISSION_STRATEGY.get());
        }
    }
}
