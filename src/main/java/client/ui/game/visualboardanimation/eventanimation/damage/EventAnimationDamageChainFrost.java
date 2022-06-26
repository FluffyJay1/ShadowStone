package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Game;
import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationA;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.ParticleSystem;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.card.Card;
import server.event.EventDamage;

import java.util.List;
import java.util.function.Supplier;

// special animation for the chain frost spell
// assumes exactly 1 target
public class EventAnimationDamageChainFrost extends EventAnimationDamage {
    private static final Supplier<Image> PROJECTILE_IMAGE = () -> Game.getImage("res/particle/attack/chainfrost.png");
    private static final double PROJECTILE_SPEED = 1000;
    public static final Supplier<EmissionStrategy> TRAIL = () -> new EmissionStrategy(
            new IntervalEmissionTimingStrategy(1, 0.03),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/chainfrosttrail.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.7)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0, new Vector2f(),
                            () -> new QuadraticInterpolationB(1, 0, 0),
                            () -> new LinearInterpolation(2, 0)
                    ),
                    new RandomAngleEmissionPropertyStrategy(new ConstantInterpolation(0))
            ))
    );

    public static final Supplier<EmissionStrategy> HIT = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(25),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/chainfrostimpact.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.25, 0.5)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.03, new Vector2f(),
                            () -> new QuadraticInterpolationB(0.3, 0, 0),
                            () -> new QuadraticInterpolationA(0.3, 1.3 + Math.random(), -2)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(200, 1200)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
    );
    private static Card lastCardSource = null;

    private static Vector2f startPos;

    private ParticleSystem trail;

    public EventAnimationDamageChainFrost() {
        super(0.5, true);
    }

    @Override
    public void onStart() {
        if (this.event.cardSource != lastCardSource) {
            lastCardSource = this.event.cardSource;
            startPos = lastCardSource.uiCard.getPos();
        }
        Vector2f startAbs = this.visualBoard.uiBoard.getAbsPosOfLocal(startPos);
        Vector2f targetAbs = this.event.m.get(0).uiCard.getAbsPos();
        // compute speed
        this.preTime = startAbs.distance(targetAbs) / PROJECTILE_SPEED;

        this.trail = this.visualBoard.uiBoard.addParticleSystem(this.getProjectilePos(), UIBoard.PARTICLE_Z_BOARD, TRAIL.get());
        this.trail.setMoveWithParticles(false);
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        this.trail.setPos(this.getProjectilePos(), 1);
    }

    @Override
    public void onProcess() {
        startPos = this.event.m.get(0).uiCard.getPos();
        this.visualBoard.uiBoard.addParticleSystem(startPos, UIBoard.PARTICLE_Z_BOARD, HIT.get());
        this.trail.kill();
    }

    private Vector2f getProjectilePos() {
        Vector2f target = this.event.m.get(0).uiCard.getPos();
        return startPos.copy().add(target.sub(startPos).scale((float) this.normalizedPre()));
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            drawCenteredAndScaled(g, PROJECTILE_IMAGE.get(), this.visualBoard.uiBoard.getAbsPosOfLocal(this.getProjectilePos()), 1, 1);
        } else {
            this.drawDamageNumber(g);
        }
    }
}
