package client.ui.game.visualboardanimation.eventanimation.damage;

import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.meta.SequentialInterpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.meta.ScaledEmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationDamageClaw extends EventAnimationDamage {
    private static final Supplier<EmissionStrategy> CLAW_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(1),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> {
                        Animation anim = new Animation("res/particle/attack/claw.png", new Vector2f(4, 3), 0, 0);
                        anim.play = true;
                        anim.setFrameInterval(0.025);
                        return anim;
                    }),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(1)),
                    new ConstantEmissionPropertyStrategy(Graphics.MODE_NORMAL, 0, new Vector2f(0, 0),
                            () -> new SequentialInterpolation<>(List.of(
                                    new ConstantInterpolation(1),
                                    new QuadraticInterpolationB(1, 0, 0)
                            ), List.of(0.5, 1.)),
                            () -> new ConstantInterpolation(0.8)
                    ),
                    new RandomAngleEmissionPropertyStrategy(new ConstantInterpolation(0))
            ))
    );
    private static final double BIG_CLAW_SCALE = 1.5;

    public EventAnimationDamageClaw() {
        super(0.25, true);
    }

    @Override
    public void onStart() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(),UIBoard.PARTICLE_Z_BOARD,
                    new ScaledEmissionStrategy(CLAW_EMISSION_STRATEGY.get(), m.uiCard.getScale()));
        }
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(),UIBoard.PARTICLE_Z_BOARD,
                    new ScaledEmissionStrategy(CLAW_EMISSION_STRATEGY.get(), BIG_CLAW_SCALE * m.uiCard.getScale()));
        }
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
