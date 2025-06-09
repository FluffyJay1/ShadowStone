package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Game;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;

import java.util.List;
import java.util.function.Supplier;

public class EventAnimationDamageIceFall extends EventAnimationDamage {
    // these must be suppliers to avoid ExceptionInInitializerError
    private static final Supplier<Image> ICE_IMAGE = () -> Game.getImage("particle/attack/icecrystal.png");

    private static final Interpolation<Double> ICE_FALL_OFFSET = new QuadraticInterpolationA(-175, -15, -15);
    private static final Interpolation<Double> ICE_FALL_SCALE = new QuadraticInterpolationA(2.5, 1.2, -0.5);
    private static final Interpolation<Double> ICE_FALL_ALPHA = new QuadraticInterpolationA(0, 1, 0.4);

    public static final Supplier<EmissionStrategy> HIT = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(15),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/attack/chainfrostimpact.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.25, 0.5)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.03, new Vector2f(),
                            () -> new QuadraticInterpolationB(0.3, 0, 0),
                            () -> new QuadraticInterpolationA(0.3, 0.6 + Math.random(), -2)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(200, 800)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
    );

    public EventAnimationDamageIceFall() {
        super(0.2, true);
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, HIT.get());
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            for (Minion m : this.event.m) {
                UICard uic = m.uiCard;
                Vector2f drawPos = uic.getAbsPos();
                drawPos.y += ICE_FALL_OFFSET.get(this.normalizedPre()).floatValue();
                drawCenteredAndScaled(g, ICE_IMAGE.get(), drawPos,
                        ICE_FALL_SCALE.get(this.normalizedPre()).floatValue(),
                        ICE_FALL_ALPHA.get(this.normalizedPre()).floatValue(),
                        0);
            }
        } else {
            this.drawDamageNumber(g);
        }
    }
}
