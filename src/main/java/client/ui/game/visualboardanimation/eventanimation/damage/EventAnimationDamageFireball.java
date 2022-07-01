package client.ui.game.visualboardanimation.eventanimation.damage;

import client.Game;
import client.VisualBoard;
import client.ui.Animation;
import client.ui.game.UIBoard;
import client.ui.interpolation.Interpolation;
import client.ui.interpolation.realvalue.*;
import client.ui.particle.ParticleSystem;
import client.ui.particle.strategy.EmissionStrategy;
import client.ui.particle.strategy.property.*;
import client.ui.particle.strategy.timing.InstantEmissionTimingStrategy;
import client.ui.particle.strategy.timing.IntervalEmissionTimingStrategy;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.geom.Vector2f;
import server.card.Minion;
import server.event.EventDamage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EventAnimationDamageFireball extends EventAnimationDamage {
    private static final Supplier<Image> PROJECTILE_IMAGE = () -> Game.getImage("res/particle/attack/fireball.png");
    public static final Supplier<EmissionStrategy> TRAIL = () -> new EmissionStrategy(
            new IntervalEmissionTimingStrategy(1, 0.04),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/attack/fireballtrail.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new ConstantInterpolation(0.6)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.5, new Vector2f(),
                            () -> new QuadraticInterpolationB(0.8, 0, 0),
                            () -> new LinearInterpolation(2 + Math.random() * 0.5, 1)
                    ),
                    new RadialVelocityEmissionPropertyStrategy(new LinearInterpolation(0, 100)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-300, 300))
            ))
    );

    public static final Supplier<EmissionStrategy> HIT = () -> new EmissionStrategy(
            new InstantEmissionTimingStrategy(10),
            new ComposedEmissionPropertyStrategy(List.of(
                    new AnimationEmissionPropertyStrategy(() -> new Animation("res/particle/misc/fire.png", new Vector2f(1, 1), 0, 0)),
                    new MaxTimeEmissionPropertyStrategy(new LinearInterpolation(0.5, 0.9)),
                    new ConstantEmissionPropertyStrategy(
                            Graphics.MODE_ADD, 0.65, new Vector2f(0, 0),
                            () -> new QuadraticInterpolationB(1, 0, -2),
                            () -> new QuadraticInterpolationB(1, 3, 4)
                    ),
                    new CirclePositionEmissionPropertyStrategy(20),
                    new RadialVelocityEmissionPropertyStrategy(new QuadraticInterpolationB(0, 350, 0)),
                    new RandomAngleEmissionPropertyStrategy(new LinearInterpolation(-100, 100))
            ))
    );

    private static final double CHARGE_TIME = 0.5;
    private static final double FIRE_TIME = 0.25;
    private static final Interpolation<Double> PROJECTILE_TIME_INTERP = new ClampedInterpolation(CHARGE_TIME / (CHARGE_TIME + FIRE_TIME), 1);

    private Map<Minion, ParticleSystem> trails;

    public EventAnimationDamageFireball() {
        super(CHARGE_TIME + FIRE_TIME, true);
    }

    @Override
    public void init(VisualBoard b, EventDamage event) {
        super.init(b, event);
        this.trails = new HashMap<>();
    }

    @Override
    public void onStart() {
        for (Minion m : this.event.m) {
            ParticleSystem ps = this.visualBoard.uiBoard.addParticleSystem(this.event.cardSource.uiCard.getPos(), UIBoard.PARTICLE_Z_BOARD, TRAIL.get());
            ps.setMoveWithParticles(false);
            this.trails.put(m, ps);
        }
    }

    @Override
    public void update(double frametime) {
        super.update(frametime);
        for (Map.Entry<Minion, ParticleSystem> entry : this.trails.entrySet()) {
            entry.getValue().setPos(this.interpProjectile(entry.getKey().uiCard.getPos(), PROJECTILE_TIME_INTERP.get(this.normalizedPre()).floatValue()), 1);
        }
    }

    @Override
    public void onProcess() {
        for (ParticleSystem ps : this.trails.values()) {
            ps.kill();
        }
        for (Minion m : this.event.m) {
            this.visualBoard.uiBoard.addParticleSystem(m.uiCard.getPos(),UIBoard.PARTICLE_Z_BOARD, HIT.get());
        }
    }

    @Override
    public void draw(Graphics g) {
        if (this.isPre()) {
            this.drawProjectile(g, PROJECTILE_IMAGE.get(), PROJECTILE_TIME_INTERP.get(this.normalizedPre()).floatValue());
        } else {
            this.drawDamageNumber(g);
        }
    }
}
