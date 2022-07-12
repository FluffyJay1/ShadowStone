package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Game;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;

import java.util.List;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.newdawn.slick.opengl.renderer.SGL.GL_ONE;
import static org.newdawn.slick.opengl.renderer.SGL.GL_SRC_ALPHA;

public class EventAnimationDamageOrbFall extends EventAnimationDamage {
    // these must be suppliers to avoid ExceptionInInitializerError
    private static final Supplier<Image> ORB_IMAGE = () -> Game.getImage("particle/attack/orb.png");

    private static final Interpolation<Double> ORB_FALL_OFFSET = new QuadraticInterpolationA(-150, 0, -15);
    private static final Interpolation<Double> ORB_FALL_SCALE = new QuadraticInterpolationA(2.5, 1.2, -0.5);
    private static final Interpolation<Double> ORB_FALL_ALPHA = new QuadraticInterpolationA(0, 1, 0.4);

    private static final Supplier<EmissionStrategy> ORB_EXPLOSION = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(4),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/attack/orbfragment.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.3, 0.5)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.02, new Vector2f(0, 800),
                            () -> new LinearInterpolation(1, 0),
                            () -> new QuadraticInterpolationA(1.5, 0.6, -0.5)
                    ),
                    new DirectionalVelocityUniformSpreadEmissionPropertyStrategy(new Vector2f(1, 0), new ConstantInterpolation(2 * Math.PI), new ConstantInterpolation(400), 4),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-200, 200))
            ))
    );

    public EventAnimationDamageOrbFall() {
        super(0.2, true);
    }

    @Override
    public void onProcess() {
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, ORB_EXPLOSION.get());
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            for (Minion m : this.event.m) {
                UICard uic = m.uiCard;
                Vector2f drawPos = uic.getAbsPos();
                drawPos.y += ORB_FALL_OFFSET.get(this.normalizedPre()).floatValue();
                g.setDrawMode(Graphics.MODE_ADD);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE); // major weirdchamp on you slick
                drawCenteredAndScaled(g, ORB_IMAGE.get(), drawPos,
                        ORB_FALL_SCALE.get(this.normalizedPre()).floatValue(),
                        ORB_FALL_ALPHA.get(this.normalizedPre()).floatValue());
                g.setDrawMode(Graphics.MODE_NORMAL);
            }
        } else {
            this.drawDamageNumber(g);
        }
    }
}
