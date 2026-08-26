package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Game;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.game.UICard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.meta.SequentialInterpolation;
import client.ui.interpolation.realvalue.ConstantInterpolation;
import client.ui.interpolation.realvalue.LinearInterpolation;
import client.ui.interpolation.realvalue.QuadraticInterpolationB;
import client.ui.particle.ParticleSystem;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;

import java.util.List;
import java.util.function.Supplier;

// for explosions that damage adjacent minions
// assumes the center of the explosion is the 0th target
public class EventAnimationDamageWidowMineExplosion extends EventAnimationDamage {
    private static final Supplier<Image> PROJECTILE_IMAGE = () -> Game.getImage("particle/attack/rocket.png");
    public static final Supplier<EmissionStrategy> TRAIL = () -> new EmissionStrategy(
            new IntervalEmissionTimingStrategy(1, 0.01),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/attack/fireballtrail.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.2)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.5, new Vector2f(),
                            () -> new QuadraticInterpolationB(0.8, 0, 0),
                            () -> new LinearInterpolation(1 + Math.random() * 0.5, 0.5)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 20)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
    );
    private static final Supplier<EmissionStrategy> EXPLOSION_EMISSION_STRATEGY = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(20),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("particle/misc/fire.png", new Vector2f(1, 1), 0, 0)),
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

    private static final double CHARGE_TIME = .3;
    private static final double FIRE_TIME = .1;
    private static final double FIRE_START_NORMALIZED = CHARGE_TIME / (CHARGE_TIME + FIRE_TIME);
    private static final Interpolation<Double> CHARGE_TIME_INTERP = new SequentialInterpolation<>(List.of(new QuadraticInterpolationB(0, 1, 2.5), new ConstantInterpolation(1)), List.of(0.75, 0.25));
    private static final float RISE_AMOUNT = 150;
    private static final float BASE_SCALE = 0.5f;
    private static final float PEAK_SCALE_DIFF = 1;

    private ParticleSystem trail;

    public EventAnimationDamageWidowMineExplosion() {
        super(CHARGE_TIME + FIRE_TIME, true);
    }

    @Override
    public void onProcess() {
        if (this.event.m.size() > 0) {
            this.trail.kill();
            UICard c = this.event.m.get(0).uiCard;
            this.visualBoard.uiBoard.addParticleSystem(c.getPos(), UIBoard.PARTICLE_Z_BOARD, EXPLOSION_EMISSION_STRATEGY.get());
        }
    }

    @Override
    public void onStart() {
        if (this.event.m.size() > 0) {
            ParticleSystem ps = this.visualBoard.uiBoard.addParticleSystem(this.event.cardSource.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, TRAIL.get());
            ps.setMoveWithParticles(false);
            this.trail = ps;
        }
    }

    private Vector2f getRocketPos(double time) {
        Vector2f pos;
        if (this.normalizedPre() < FIRE_START_NORMALIZED) {
            float interp = CHARGE_TIME_INTERP.get(this.normalizedPre() / FIRE_START_NORMALIZED).floatValue();
            pos = this.event.cardSource.uiCard.getPos().copy();
            pos.y -= RISE_AMOUNT * interp;
        } else {
            float interp = (float) ((this.normalizedPre() - FIRE_START_NORMALIZED) / (1 - FIRE_START_NORMALIZED));
            Vector2f start = this.event.cardSource.uiCard.getPos().copy();
            start.y -= RISE_AMOUNT;
            Vector2f diff = this.event.m.get(0).uiCard.getPos().copy().sub(start);
            pos = start.add(diff.scale(interp));
        }
        return pos;
    }

    private float getRocketScale(double time) {
        if (this.normalizedPre() < FIRE_START_NORMALIZED) {
            float interp = CHARGE_TIME_INTERP.get(this.normalizedPre() / FIRE_START_NORMALIZED).floatValue();
            return BASE_SCALE + PEAK_SCALE_DIFF * interp;
        } else {
            float interp = (float) ((this.normalizedPre() - FIRE_START_NORMALIZED) / (1 - FIRE_START_NORMALIZED));
            return BASE_SCALE + PEAK_SCALE_DIFF * (1 - interp);
        }
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        if (this.event.m.size() > 0) {
            this.trail.setPos(this.getRocketPos(this.normalizedPre()), 1);
            this.trail.setEmissionScale(this.getRocketScale(this.normalizedPre()));
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        if (this.isPre() && this.event.m.size() > 0) {
            float angle;
            if (this.normalizedPre() < FIRE_START_NORMALIZED) {
                angle = -90;
            } else {
                Vector2f risenPos = this.event.cardSource.uiCard.getAbsPos().copy();
                risenPos.y -= RISE_AMOUNT;
                Vector2f diff = this.event.m.get(0).uiCard.getAbsPos().copy().sub(risenPos);
                double rad = Math.atan2(diff.y, diff.x);
                angle = (float) (rad * 180 / Math.PI);
            }
            drawCenteredAndScaled(g, PROJECTILE_IMAGE.get(), this.visualBoard.uiBoard.getAbsOfPos(this.getRocketPos(this.normalizedPre())), this.getRocketScale(this.normalizedPre()), 1, angle);
        }
    }
}
